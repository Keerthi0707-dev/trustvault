// TrustVault Secure API Client

function getCsrfToken() {
  const match = document.cookie.match(new RegExp('(^|;\\s*)XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

export async function apiRequest(endpoint, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = { ...options.headers };

  // Attach CSRF token on state-changing requests
  if (['POST', 'PATCH', 'PUT', 'DELETE'].includes(method)) {
    const csrf = getCsrfToken();
    if (csrf) {
      headers['X-XSRF-TOKEN'] = csrf;
    }
  }

  // Don't set Content-Type if sending FormData (browser handles boundary)
  if (!(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(endpoint, {
    ...options,
    method,
    headers,
    credentials: 'include', // send session cookies
  });

  if (response.status === 401 && !endpoint.includes('/api/auth/login')) {
    // Session expired or unauthenticated
    window.dispatchEvent(new CustomEvent('auth:expired'));
  }

  if (!response.ok) {
    let errorMessage = `Request failed (${response.status})`;
    try {
      const errJson = await response.json();
      errorMessage = errJson.message || errJson.error || errorMessage;
    } catch {
      // response is not JSON
    }
    const error = new Error(errorMessage);
    error.status = response.status;
    throw error;
  }

  // Handle empty responses (like 204 or empty OK)
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return await response.json();
  }

  return response;
}

export const api = {
  // Auth
  login: (email, password) =>
    apiRequest('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  register: (email, password) =>
    apiRequest('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  logout: () => apiRequest('/api/auth/logout', { method: 'POST' }),
  getMe: () => apiRequest('/api/auth/me'),

  // Users
  searchUsers: (query) =>
    apiRequest(`/api/users${query ? `?search=${encodeURIComponent(query)}` : ''}`),

  // Documents
  listDocuments: (search = '', category = '', startDate = '', endDate = '') => {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (category) params.append('category', category);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const qs = params.toString();
    return apiRequest(`/api/documents${qs ? `?${qs}` : ''}`);
  },
  getDocument: (id) => apiRequest(`/api/documents/${id}`),
  uploadDocument: (formData) =>
    apiRequest('/api/documents', {
      method: 'POST',
      body: formData,
    }),
  updateDocument: (id, data) =>
    apiRequest(`/api/documents/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),
  deleteDocument: (id) =>
    apiRequest(`/api/documents/${id}`, {
      method: 'DELETE',
    }),
  downloadDocumentUrl: (id) => `/api/documents/${id}/download`,
  getDocumentAudit: (id) => apiRequest(`/api/documents/${id}/audit`),
  getAiAccessSuggestion: (id) => apiRequest(`/api/documents/${id}/ai-suggest-access`),

  // Grants
  listGrants: (documentId) =>
    apiRequest(`/api/documents/${documentId}/grants`),
  createGrant: (documentId, recipientEmail, permission, aiAssisted = false, aiRationale = null) =>
    apiRequest(`/api/documents/${documentId}/grants`, {
      method: 'POST',
      body: JSON.stringify({ recipientEmail, permission, aiAssisted, aiRationale }),
    }),
  revokeGrant: (documentId, grantId) =>
    apiRequest(`/api/documents/${documentId}/grants/${grantId}`, {
      method: 'DELETE',
    }),

  // User Activity
  getActivity: () => apiRequest('/api/activity'),

  // Admin
  getAdminUsers: () => apiRequest('/api/admin/users'),
  updateUserStatus: (userId, status) =>
    apiRequest(`/api/admin/users/${userId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),
  getAdminAudit: () => apiRequest('/api/admin/audit'),
  checkAuditIntegrity: () => apiRequest('/api/admin/audit/integrity'),
};
