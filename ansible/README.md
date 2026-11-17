# Ansible application deployment

This playbook deploys the existing runnable Spring Boot JAR, rather than only
writing configuration. The reusable `roles/inventory_app` role installs Java 17,
creates an unprivileged service account, copies the JAR and configuration, and
enables a systemd service. A notified handler restarts it only when the JAR,
configuration, or unit changes. The final task waits for HTTP 200 and `status: UP`
at `/api/health`.

## Supported target and permissions

Use Debian 12 or Ubuntu 22.04/24.04 with Python 3, apt, and systemd running as PID 1.
The supplied `inventory.ini` targets localhost using a local connection; this means
**the Linux machine running Ansible is the deployment target**. Do not run it on a
machine where you do not intend to install and start the application.

On Windows use Ubuntu WSL2, not native PowerShell, for Ansible. In WSL, check:

```bash
ps -p 1 -o comm=
systemctl is-system-running
```

PID 1 must be `systemd`. If necessary, add `[boot]` and `systemd=true` to
`/etc/wsl.conf` (preserving existing settings), then run `wsl --shutdown` from
PowerShell and reopen Ubuntu. An older WSL installation may need `wsl --update`.
Inspect any degraded systemd state before deployment. Ordinary containers without
systemd are unsupported.

The connecting user needs sudo access to install packages, manage users/groups,
write `/opt`, `/etc`, and systemd units, and start/restart/enable services. The
playbook uses `become: true`; `--ask-become-pass` prompts for the sudo password.
Never store passwords in the inventory or Git. The application itself runs as
`inventory-app`, without a login shell or root privileges. Use a port above 1023
with this unprivileged service. Ensure the selected port is free.

## Build and deploy

From the repository root in Ubuntu/WSL (use your own clone location):

```bash
sudo apt update
sudo apt install -y ansible maven openjdk-17-jdk
mvn clean package
ansible-inventory -i ansible/inventory.ini --graph
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --syntax-check
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --ask-become-pass
```

The JAR is built on the Ansible controller before deployment. Defaults are in
`roles/inventory_app/defaults/main.yml`; override them without editing the role:

```bash
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --ask-become-pass \
  -e app_port=9090 -e app_environment=staging
```

`app_jar_src` is a controller-side path, defaulting to the Maven artifact under
`target/`. `app_dir` defaults to `/opt/inventory-management`, and `config_dir` to
`/etc/inventory-management`. Keep directory overrides absolute and free of spaces
because systemd uses them in `ExecStart`. `app_name`, `app_user`, and `app_group`
should stay stable after first installation; changing them does not remove old
services/accounts. The current play targets the `local` inventory group; for a
remote target, replace its localhost entry with an SSH host in that group and
configure SSH access separately.

## Verify the service, health, restart handler, and idempotence

```bash
systemctl status inventory-management-system --no-pager
systemctl is-enabled inventory-management-system
sudo journalctl -u inventory-management-system -n 50 --no-pager
curl --fail http://localhost:8080/api/health
curl --fail http://localhost:8080/api/products
# Open http://localhost:8080/ to check the existing dashboard.
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --ask-become-pass
```

For an unchanged artifact and variables, the immediate second run should report
`changed=0` and `failed=0`, without running the restart handler. Apt cache refresh
can report a change once its one-hour cache window expires. Capture the actual
recap rather than assuming idempotence. Deploy with `-e app_port=9090` to verify
the restart handler and health check use the new port, then repeat with that same
override to verify no further restart. Restore port 8080 by running with defaults.
Changes restart the in-memory application, so inventory edits are lost, as they
are on any application restart. `--check --diff` can preview changes but cannot
prove service startup on a fresh target.

## Verification performed for this PR

- Ansible syntax check and inventory parsing passed in the Codex Linux environment.
- Full installation, systemd startup, handler execution, and second-run
  idempotence require the supported target above; the Codex environment has no
  systemd PID 1, so these are **MANUAL_VERIFICATION_REQUIRED**.
