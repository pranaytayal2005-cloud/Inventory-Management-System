/**
 * Inventory Management System - Frontend Application Logic
 * Communicates with Spring Boot REST API at /api/products and /api/health
 */

// Application State
const state = {
  products: [],
  filteredProducts: [],
  categories: new Set(),
  activeStockFilter: "all", // 'all', 'low', 'in', 'out'
  selectedCategory: "all",
  searchQuery: "",
  sortBy: "id-asc",
  editingProductId: null,
  deletingProductId: null,
  isLoading: false,
};

// DOM Element References
const elements = {
  healthPill: document.getElementById("healthPill"),
  healthText: document.getElementById("healthText"),
  metricTotalProducts: document.getElementById("metricTotalProducts"),
  metricTotalStock: document.getElementById("metricTotalStock"),
  metricLowStock: document.getElementById("metricLowStock"),
  metricValuation: document.getElementById("metricValuation"),
  searchInput: document.getElementById("searchInput"),
  categoryFilter: document.getElementById("categoryFilter"),
  stockFilterBtn: document.getElementById("stockFilterBtn"),
  sortSelect: document.getElementById("sortSelect"),
  productsTableBody: document.getElementById("productsTableBody"),
  tableLoading: document.getElementById("tableLoading"),
  tableEmpty: document.getElementById("tableEmpty"),
  productCountLabel: document.getElementById("productCountLabel"),
  refreshBtn: document.getElementById("refreshBtn"),
  addProductBtn: document.getElementById("addProductBtn"),

  // Product Modal Elements
  productModal: document.getElementById("productModal"),
  productModalTitle: document.getElementById("productModalTitle"),
  productForm: document.getElementById("productForm"),
  productIdInput: document.getElementById("productId"),
  nameInput: document.getElementById("productName"),
  categoryInput: document.getElementById("productCategory"),
  categoryDatalist: document.getElementById("categoryDatalist"),
  priceInput: document.getElementById("productPrice"),
  quantityInput: document.getElementById("productQuantity"),
  descriptionInput: document.getElementById("productDescription"),
  productModalClose: document.getElementById("productModalClose"),
  productModalCancel: document.getElementById("productModalCancel"),
  saveProductBtn: document.getElementById("saveProductBtn"),

  // Delete Modal Elements
  deleteModal: document.getElementById("deleteModal"),
  deleteProductName: document.getElementById("deleteProductName"),
  deleteModalClose: document.getElementById("deleteModalClose"),
  deleteModalCancel: document.getElementById("deleteModalCancel"),
  confirmDeleteBtn: document.getElementById("confirmDeleteBtn"),

  // Toast Container
  toastContainer: document.getElementById("toastContainer"),
};

// Initialize Application
document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners();
  loadInitialData();
  // Periodic health check every 30 seconds
  setInterval(checkHealth, 30000);
});

// Event Listeners Registration
function setupEventListeners() {
  // Search and Filters
  elements.searchInput.addEventListener("input", (e) => {
    state.searchQuery = e.target.value.trim().toLowerCase();
    applyFiltersAndRender();
  });

  elements.categoryFilter.addEventListener("change", (e) => {
    state.selectedCategory = e.target.value;
    applyFiltersAndRender();
  });

  elements.stockFilterBtn.addEventListener("click", () => {
    // Toggle low-stock filter
    if (state.activeStockFilter === "low") {
      state.activeStockFilter = "all";
      elements.stockFilterBtn.classList.remove("active");
    } else {
      state.activeStockFilter = "low";
      elements.stockFilterBtn.classList.add("active");
    }
    applyFiltersAndRender();
  });

  elements.sortSelect.addEventListener("change", (e) => {
    state.sortBy = e.target.value;
    applyFiltersAndRender();
  });

  elements.refreshBtn.addEventListener("click", () => {
    loadInitialData(true);
  });

  // Modal Open/Close
  elements.addProductBtn.addEventListener("click", () => openProductModal("add"));
  elements.productModalClose.addEventListener("click", closeProductModal);
  elements.productModalCancel.addEventListener("click", closeProductModal);
  elements.productForm.addEventListener("submit", handleProductFormSubmit);

  elements.deleteModalClose.addEventListener("click", closeDeleteModal);
  elements.deleteModalCancel.addEventListener("click", closeDeleteModal);
  elements.confirmDeleteBtn.addEventListener("click", confirmDeleteProduct);

  // Close modals when clicking backdrop
  window.addEventListener("click", (e) => {
    if (e.target === elements.productModal) closeProductModal();
    if (e.target === elements.deleteModal) closeDeleteModal();
  });

  // Keyboard shortcut Esc to close open modals
  window.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      closeProductModal();
      closeDeleteModal();
    }
  });
}

// ==========================================
// API Interaction Functions
// ==========================================

async function loadInitialData(isRefresh = false) {
  setLoading(true);
  try {
    await Promise.all([checkHealth(), fetchProducts()]);
    if (isRefresh) {
      showToast("Inventory data refreshed", "success");
    }
  } catch (error) {
    console.error("Failed to load initial inventory data:", error);
    showToast("Failed to connect to Inventory API. Is the server running?", "error");
  } finally {
    setLoading(false);
  }
}

/**
 * Checks system health via GET /api/health
 */
async function checkHealth() {
  try {
    const response = await fetch("/api/health");
    if (!response.ok) throw new Error(`Health status: ${response.status}`);
    const data = await response.json();
    if (data.status === "UP") {
      elements.healthPill.className = "health-pill healthy";
      elements.healthText.textContent = `Online (v${data.version || "1.0.0"})`;
    } else {
      elements.healthPill.className = "health-pill unhealthy";
      elements.healthText.textContent = "Service Degraded";
    }
  } catch (err) {
    elements.healthPill.className = "health-pill unhealthy";
    elements.healthText.textContent = "Offline";
  }
}

/**
 * Fetches all products via GET /api/products
 */
async function fetchProducts() {
  const response = await fetch("/api/products");
  if (!response.ok) {
    throw new Error(`Failed to fetch products: ${response.statusText}`);
  }
  const products = await response.json();
  state.products = products;
  updateCategoriesList();
  updateMetrics();
  applyFiltersAndRender();
}

/**
 * Creates a new product via POST /api/products
 */
async function createProduct(productData) {
  const response = await fetch("/api/products", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(productData),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw errorData;
  }
  return await response.json();
}

/**
 * Updates an existing product via PUT /api/products/{id}
 */
async function updateProduct(id, productData) {
  const response = await fetch(`/api/products/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(productData),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw errorData;
  }
  return await response.json();
}

/**
 * Deletes a product via DELETE /api/products/{id}
 */
async function deleteProduct(id) {
  const response = await fetch(`/api/products/${id}`, {
    method: "DELETE",
  });

  if (!response.ok && response.status !== 204) {
    const errorData = await response.json().catch(() => ({}));
    throw errorData;
  }
}

// ==========================================
// Filtering, Sorting, and Metrics Logic
// ==========================================

function updateCategoriesList() {
  state.categories = new Set();
  state.products.forEach((p) => {
    if (p.category) state.categories.add(p.category.trim());
  });

  // Preserve current selection if possible
  const currentSelection = elements.categoryFilter.value;
  elements.categoryFilter.innerHTML = '<option value="all">All Categories</option>';
  elements.categoryDatalist.innerHTML = "";

  Array.from(state.categories).sort().forEach((cat) => {
    const option = document.createElement("option");
    option.value = cat;
    option.textContent = cat;
    elements.categoryFilter.appendChild(option);

    const datalistOption = document.createElement("option");
    datalistOption.value = cat;
    elements.categoryDatalist.appendChild(datalistOption);
  });

  if (state.categories.has(currentSelection)) {
    elements.categoryFilter.value = currentSelection;
    state.selectedCategory = currentSelection;
  } else {
    elements.categoryFilter.value = "all";
    state.selectedCategory = "all";
  }
}

function updateMetrics() {
  const totalCount = state.products.length;
  let totalStock = 0;
  let lowStockCount = 0;
  let totalValuation = 0;

  state.products.forEach((p) => {
    const qty = Number(p.quantity) || 0;
    const price = Number(p.price) || 0;
    totalStock += qty;
    if (qty <= 10) {
      lowStockCount++;
    }
    totalValuation += qty * price;
  });

  elements.metricTotalProducts.textContent = totalCount.toLocaleString();
  elements.metricTotalStock.textContent = totalStock.toLocaleString();
  elements.metricLowStock.textContent = lowStockCount.toLocaleString();
  elements.metricValuation.textContent = formatCurrency(totalValuation);
}

function applyFiltersAndRender() {
  let list = [...state.products];

  // 1. Category Filter
  if (state.selectedCategory !== "all") {
    list = list.filter((p) => p.category === state.selectedCategory);
  }

  // 2. Stock Level Filter
  if (state.activeStockFilter === "low") {
    list = list.filter((p) => p.quantity <= 10);
  }

  // 3. Search Query Filter
  if (state.searchQuery) {
    const query = state.searchQuery;
    list = list.filter((p) => {
      const idMatch = String(p.id).includes(query);
      const nameMatch = p.name && p.name.toLowerCase().includes(query);
      const descMatch = p.description && p.description.toLowerCase().includes(query);
      const catMatch = p.category && p.category.toLowerCase().includes(query);
      return idMatch || nameMatch || descMatch || catMatch;
    });
  }

  // 4. Sorting
  list.sort((a, b) => {
    switch (state.sortBy) {
      case "id-asc":
        return a.id - b.id;
      case "id-desc":
        return b.id - a.id;
      case "name-asc":
        return (a.name || "").localeCompare(b.name || "");
      case "name-desc":
        return (b.name || "").localeCompare(a.name || "");
      case "price-asc":
        return (Number(a.price) || 0) - (Number(b.price) || 0);
      case "price-desc":
        return (Number(b.price) || 0) - (Number(a.price) || 0);
      case "qty-asc":
        return (Number(a.quantity) || 0) - (Number(b.quantity) || 0);
      case "qty-desc":
        return (Number(b.quantity) || 0) - (Number(a.quantity) || 0);
      default:
        return a.id - b.id;
    }
  });

  state.filteredProducts = list;
  renderTable();
}

// ==========================================
// Rendering Table Rows
// ==========================================

function renderTable() {
  elements.productsTableBody.innerHTML = "";
  const total = state.products.length;
  const filtered = state.filteredProducts.length;

  if (total === 0) {
    elements.productCountLabel.textContent = "0 products";
    elements.tableEmpty.style.display = "block";
    elements.productsTableBody.style.display = "none";
    return;
  }

  if (filtered === 0) {
    elements.productCountLabel.textContent = `0 of ${total} products matching filters`;
    elements.tableEmpty.style.display = "block";
    elements.productsTableBody.style.display = "none";
    return;
  }

  elements.tableEmpty.style.display = "none";
  elements.productsTableBody.style.display = "";
  elements.productCountLabel.textContent = `Showing ${filtered} of ${total} products`;

  const fragment = document.createDocumentFragment();

  state.filteredProducts.forEach((product) => {
    const tr = document.createElement("tr");

    // Stock status badge determination
    const qty = Number(product.quantity) || 0;
    const price = Number(product.price) || 0;
    const itemValuation = qty * price;

    let stockBadgeClass = "stock-in";
    let stockBadgeLabel = `${qty} in stock`;
    if (qty === 0) {
      stockBadgeClass = "stock-out";
      stockBadgeLabel = "Out of stock";
    } else if (qty <= 10) {
      stockBadgeClass = "stock-low";
      stockBadgeLabel = `Low: ${qty} left`;
    }

    tr.innerHTML = `
      <td><strong>#${product.id}</strong></td>
      <td class="product-name-cell">
        <span class="product-name">${escapeHtml(product.name)}</span>
        <span class="product-desc" title="${escapeHtml(product.description || "")}">
          ${escapeHtml(product.description || "No description")}
        </span>
      </td>
      <td>
        <span class="category-badge">${escapeHtml(product.category || "Uncategorized")}</span>
      </td>
      <td><strong>${formatCurrency(price)}</strong></td>
      <td>
        <span class="stock-badge ${stockBadgeClass}">
          ${stockBadgeLabel}
        </span>
      </td>
      <td>${formatCurrency(itemValuation)}</td>
      <td>
        <div class="action-buttons">
          <button class="btn-icon edit" title="Edit product" data-id="${product.id}">
            ✏️
          </button>
          <button class="btn-icon delete" title="Delete product" data-id="${product.id}">
            🗑️
          </button>
        </div>
      </td>
    `;

    // Event listeners on action buttons
    const editBtn = tr.querySelector(".btn-icon.edit");
    editBtn.addEventListener("click", () => openProductModal("edit", product));

    const deleteBtn = tr.querySelector(".btn-icon.delete");
    deleteBtn.addEventListener("click", () => openDeleteModal(product));

    fragment.appendChild(tr);
  });

  elements.productsTableBody.appendChild(fragment);
}

// ==========================================
// Modal Handlers (Add / Edit / Delete)
// ==========================================

function openProductModal(mode = "add", product = null) {
  clearFormValidation();
  elements.productForm.reset();

  if (mode === "edit" && product) {
    state.editingProductId = product.id;
    elements.productModalTitle.textContent = `Edit Product #${product.id}`;
    elements.productIdInput.value = product.id;
    elements.nameInput.value = product.name || "";
    elements.categoryInput.value = product.category || "";
    elements.priceInput.value = product.price !== undefined ? product.price : "";
    elements.quantityInput.value = product.quantity !== undefined ? product.quantity : "";
    elements.descriptionInput.value = product.description || "";
  } else {
    state.editingProductId = null;
    elements.productModalTitle.textContent = "Add New Product";
    elements.productIdInput.value = "";
  }

  elements.productModal.classList.add("open");
  elements.nameInput.focus();
}

function closeProductModal() {
  elements.productModal.classList.remove("open");
  state.editingProductId = null;
  clearFormValidation();
}

async function handleProductFormSubmit(e) {
  e.preventDefault();
  clearFormValidation();

  const name = elements.nameInput.value.trim();
  const category = elements.categoryInput.value.trim();
  const priceStr = elements.priceInput.value.trim();
  const quantityStr = elements.quantityInput.value.trim();
  const description = elements.descriptionInput.value.trim();

  // Client-side validations
  let hasErrors = false;

  if (!name || name.length < 2 || name.length > 100) {
    setFieldError("productName", "Product name must be between 2 and 100 characters");
    hasErrors = true;
  }

  if (!category || category.length < 2 || category.length > 50) {
    setFieldError("productCategory", "Category must be between 2 and 50 characters");
    hasErrors = true;
  }

  const priceNum = parseFloat(priceStr);
  if (isNaN(priceNum) || priceNum <= 0) {
    setFieldError("productPrice", "Price must be a positive number greater than 0");
    hasErrors = true;
  }

  const quantityNum = parseInt(quantityStr, 10);
  if (isNaN(quantityNum) || quantityNum < 0) {
    setFieldError("productQuantity", "Quantity must be an integer 0 or greater");
    hasErrors = true;
  }

  if (description.length > 500) {
    setFieldError("productDescription", "Description cannot exceed 500 characters");
    hasErrors = true;
  }

  if (hasErrors) return;

  const payload = {
    name,
    category,
    price: priceNum,
    quantity: quantityNum,
    description: description || null,
  };

  elements.saveProductBtn.disabled = true;
  elements.saveProductBtn.textContent = "Saving...";

  try {
    if (state.editingProductId) {
      await updateProduct(state.editingProductId, payload);
      showToast(`Product "${name}" updated successfully`, "success");
    } else {
      await createProduct(payload);
      showToast(`Product "${name}" added to inventory`, "success");
    }
    closeProductModal();
    await fetchProducts();
  } catch (err) {
    console.error("Save product error:", err);
    if (err.validationErrors) {
      // Map server-side validation errors back to form fields
      for (const [field, message] of Object.entries(err.validationErrors)) {
        if (field === "name") setFieldError("productName", message);
        else if (field === "category") setFieldError("productCategory", message);
        else if (field === "price") setFieldError("productPrice", message);
        else if (field === "quantity") setFieldError("productQuantity", message);
        else if (field === "description") setFieldError("productDescription", message);
      }
    } else {
      showToast(err.message || "An error occurred while saving the product", "error");
    }
  } finally {
    elements.saveProductBtn.disabled = false;
    elements.saveProductBtn.textContent = "Save Product";
  }
}

function openDeleteModal(product) {
  state.deletingProductId = product.id;
  elements.deleteProductName.textContent = `${product.name} (#${product.id})`;
  elements.deleteModal.classList.add("open");
}

function closeDeleteModal() {
  elements.deleteModal.classList.remove("open");
  state.deletingProductId = null;
}

async function confirmDeleteProduct() {
  if (!state.deletingProductId) return;

  elements.confirmDeleteBtn.disabled = true;
  elements.confirmDeleteBtn.textContent = "Deleting...";

  try {
    await deleteProduct(state.deletingProductId);
    showToast("Product deleted successfully", "success");
    closeDeleteModal();
    await fetchProducts();
  } catch (err) {
    console.error("Delete failed:", err);
    showToast(err.message || "Failed to delete product", "error");
  } finally {
    elements.confirmDeleteBtn.disabled = false;
    elements.confirmDeleteBtn.textContent = "Delete";
  }
}

// ==========================================
// Form Validation & Error Utilities
// ==========================================

function setFieldError(fieldId, message) {
  const inputEl = document.getElementById(fieldId);
  if (inputEl) inputEl.classList.add("is-invalid");
  const errorEl = document.getElementById(`${fieldId}Error`);
  if (errorEl) {
    errorEl.textContent = message;
    errorEl.classList.add("visible");
  }
}

function clearFormValidation() {
  const inputs = elements.productForm.querySelectorAll(".form-control");
  inputs.forEach((input) => input.classList.remove("is-invalid"));

  const errorMessages = elements.productForm.querySelectorAll(".form-error-msg");
  errorMessages.forEach((msg) => {
    msg.textContent = "";
    msg.classList.remove("visible");
  });
}

function setLoading(isLoading) {
  state.isLoading = isLoading;
  if (isLoading) {
    elements.tableLoading.style.display = "block";
    elements.productsTableBody.style.display = "none";
    elements.tableEmpty.style.display = "none";
  } else {
    elements.tableLoading.style.display = "none";
  }
}

// ==========================================
// Toast & Format Utilities
// ==========================================

function showToast(message, type = "info") {
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;

  let icon = "ℹ️";
  if (type === "success") icon = "✅";
  else if (type === "error") icon = "❌";
  else if (type === "warning") icon = "⚠️";

  toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
  elements.toastContainer.appendChild(toast);

  // Trigger CSS animation
  requestAnimationFrame(() => toast.classList.add("show"));

  // Auto remove after 3.5 seconds
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 250);
  }, 3500);
}

function formatCurrency(amount) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
  }).format(amount);
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
