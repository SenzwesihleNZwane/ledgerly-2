const API_BASE = 'https://ledgerly-2-uzdh.onrender.com/api';

const Api = {
  token: localStorage.getItem('ledgerly_token') || null,

  setToken(token) {
    this.token = token;
    if (token) {
      localStorage.setItem('ledgerly_token', token);
    } else {
      localStorage.removeItem('ledgerly_token');
    }
  },

  async request(path, { method = 'GET', body } = {}) {
    const headers = { 'Content-Type': 'application/json' };
    if (this.token) headers['Authorization'] = `Bearer ${this.token}`;

    const res = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });

    let data = null;
    try { data = await res.json(); } catch (_) {}

    if (!res.ok) {
      const message = (data && data.message) || `Request failed (${res.status})`;
      throw new Error(message);
    }
    return data;
  },

  register(name, email, password) {
    return this.request('/auth/register', { method: 'POST', body: { name, email, password } });
  },
  login(email, password) {
    return this.request('/auth/login', { method: 'POST', body: { email, password } });
  },

  getGroups() {
    return this.request('/groups');
  },
  createGroup(name) {
    return this.request('/groups', { method: 'POST', body: { name } });
  },
  getGroup(id) {
    return this.request(`/groups/${id}`);
  },
  addMember(groupId, email) {
    return this.request(`/groups/${groupId}/members`, { method: 'POST', body: { email } });
  },

  getExpenses(groupId) {
    return this.request(`/groups/${groupId}/expenses`);
  },
  addExpense(groupId, payload) {
    return this.request(`/groups/${groupId}/expenses`, { method: 'POST', body: payload });
  },

  getBalances(groupId) {
    return this.request(`/groups/${groupId}/balances`);
  },
};