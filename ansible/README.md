# Ansible Configuration for Inventory Management System

This folder provides an automated, idempotent Ansible setup to configure the deployment environment and manage application settings for the Inventory Management System without requiring SSH.

---

## Folder Structure

```
ansible/
├── inventory.ini    # Local inventory configuration (ansible_connection=local)
├── playbook.yml     # Playbook managing directories and application configuration
└── README.md        # Execution and verification guide for Ubuntu WSL
```

---

## Execution Guide (Ubuntu WSL)

### 1. Open Ubuntu WSL Terminal
From Windows, launch your Ubuntu WSL terminal:
```powershell
wsl
```

### 2. Install Ansible (if not installed)
```bash
sudo apt update
sudo apt install -y ansible
```

Confirm the installation:
```bash
ansible --version
```

### 3. Navigate to the Ansible Directory
```bash
cd /mnt/c/Users/LENOVO/OneDrive/Desktop/Inventory-Management-System/ansible
```

### 4. Perform a Syntax Check
```bash
ansible-playbook -i inventory.ini playbook.yml --syntax-check
```

### 5. Execute the Playbook
```bash
ansible-playbook -i inventory.ini playbook.yml
```

---

## Verification

### 1. Check Generated Configuration
Inspect the contents of the generated configuration file:
```bash
cat ~/inventory-management/config/application.properties
```

Expected output:
```properties
# ==========================================
# Inventory Management System Configuration
# Managed by Ansible - Do not edit manually
# ==========================================
app.name=inventory-management-system
server.port=8080
app.environment=production
spring.application.name=inventory-management-system
inventory.default-low-stock-threshold=10
```

### 2. Verify Idempotency
Run the playbook a second time:
```bash
ansible-playbook -i inventory.ini playbook.yml
```

In the output summary:
- `changed=0`
- `failed=0`

No duplicate files or modifications are made if the configuration is already in the desired state.

---

## Custom Parameters (Optional)

You can override default settings dynamically using `-e` (extra vars):

```bash
# Example: Deploy with custom port and environment
ansible-playbook -i inventory.ini playbook.yml -e "app_port=9090 app_environment=staging"

# Example: Custom directory path
ansible-playbook -i inventory.ini playbook.yml -e "config_dir=/tmp/inventory-config"
```
