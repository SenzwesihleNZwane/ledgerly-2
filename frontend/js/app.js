// Change this to match your currency - it's just a display label.
const CURRENCY = 'R';

const state = {
  currentUser: null,
  groups: [],
  activeGroupId: null,
  activeGroup: null,
};

// ---------- small DOM helpers ----------
const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => Array.from(document.querySelectorAll(sel));

function money(amount) {
  const n = Number(amount);
  return `${CURRENCY}${n.toFixed(2)}`;
}

function showToast(message, isError = false) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.toggle('error', isError);
  toast.classList.remove('hidden');
  setTimeout(() => toast.classList.add('hidden'), 3200);
}

function openModal(id) {
  $('#modal-backdrop').classList.remove('hidden');
  $$('.modal').forEach((m) => m.classList.add('hidden'));
  $(`#${id}`).classList.remove('hidden');
}
function closeModals() {
  $('#modal-backdrop').classList.add('hidden');
}

// ---------- boot ----------
window.addEventListener('DOMContentLoaded', init);

async function init() {
  wireAuthForms();
  wireTabs();
  wireAppShell();
  wireModals();

  if (Api.token) {
    try {
      const groups = await Api.getGroups();
      // token is valid - we don't get a /me endpoint, so pull identity
      // from the token payload's stored copy in localStorage instead
      const cached = JSON.parse(localStorage.getItem('ledgerly_user') || 'null');
      if (cached) {
        enterApp(cached, groups);
        return;
      }
    } catch (e) {
      Api.setToken(null);
    }
  }
  showAuthScreen();
}

function showAuthScreen() {
  $('#auth-screen').classList.remove('hidden');
  $('#app-shell').classList.add('hidden');
}

function enterApp(user, groups) {
  state.currentUser = user;
  state.groups = groups;
  $('#auth-screen').classList.add('hidden');
  $('#app-shell').classList.remove('hidden');
  $('#current-user-name').textContent = `${user.name} · ${user.email}`;
  renderGroupList();
}

// ---------- auth ----------
function wireTabs() {
  $$('.tab-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      $$('.tab-btn').forEach((b) => b.classList.remove('active'));
      btn.classList.add('active');
      const tab = btn.dataset.tab;
      $('#login-form').classList.toggle('hidden', tab !== 'login');
      $('#register-form').classList.toggle('hidden', tab !== 'register');
    });
  });
}

function wireAuthForms() {
  $('#login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const email = $('#login-email').value.trim();
      const password = $('#login-password').value;
      const auth = await Api.login(email, password);
      afterAuth(auth);
    } catch (err) {
      showToast(err.message, true);
    }
  });

  $('#register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const name = $('#register-name').value.trim();
      const email = $('#register-email').value.trim();
      const password = $('#register-password').value;
      const auth = await Api.register(name, email, password);
      afterAuth(auth);
    } catch (err) {
      showToast(err.message, true);
    }
  });

  $('#logout-btn').addEventListener('click', () => {
    Api.setToken(null);
    localStorage.removeItem('ledgerly_user');
    state.currentUser = null;
    state.groups = [];
    state.activeGroupId = null;
    state.activeGroup = null;
    // Reset the group panel back to its empty state so a stale group
    // isn't left visible (with a now-invalid group id) after logging
    // back in as someone else.
    $('#group-view').classList.add('hidden');
    $('#empty-state').classList.remove('hidden');
    showAuthScreen();
  });
}

async function afterAuth(auth) {
  Api.setToken(auth.token);
  const user = { id: auth.id, name: auth.name, email: auth.email };
  localStorage.setItem('ledgerly_user', JSON.stringify(user));
  const groups = await Api.getGroups();
  enterApp(user, groups);
  showToast(`Welcome, ${user.name.split(' ')[0]}`);
}

// ---------- groups sidebar ----------
function renderGroupList() {
  const list = $('#group-list');
  list.innerHTML = '';
  if (state.groups.length === 0) {
    const p = document.createElement('p');
    p.className = 'empty-row';
    p.textContent = 'No groups yet.';
    list.appendChild(p);
    return;
  }
  state.groups.forEach((g) => {
    const btn = document.createElement('button');
    btn.className = 'group-item' + (g.id === state.activeGroupId ? ' active' : '');
    btn.textContent = g.name;
    btn.addEventListener('click', () => selectGroup(g.id));
    list.appendChild(btn);
  });
}

async function selectGroup(groupId) {
  state.activeGroupId = groupId;
  renderGroupList();
  $('#empty-state').classList.add('hidden');
  $('#group-view').classList.remove('hidden');

  try {
    const [group, expenses, balances] = await Promise.all([
      Api.getGroup(groupId),
      Api.getExpenses(groupId),
      Api.getBalances(groupId),
    ]);
    state.activeGroup = group;
    renderGroupHeader(group);
    renderExpenses(expenses);
    renderBalances(balances);
  } catch (err) {
    showToast(err.message, true);
  }
}

function renderGroupHeader(group) {
  $('#group-name-heading').textContent = group.name;
  const row = $('#members-row');
  row.innerHTML = '';
  group.members.forEach((m) => {
    const chip = document.createElement('span');
    chip.className = 'member-chip';
    chip.textContent = m.name;
    row.appendChild(chip);
  });
}

function renderExpenses(expenses) {
  const list = $('#expense-list');
  list.innerHTML = '';
  if (expenses.length === 0) {
    list.innerHTML = '<p class="empty-row">No expenses logged yet. Add the first one.</p>';
    return;
  }
  expenses.forEach((e) => {
    const row = document.createElement('div');
    row.className = 'expense-row';
    const date = new Date(e.createdAt).toLocaleDateString();
    row.innerHTML = `
      <div class="expense-main">
        <span class="expense-desc">${escapeHtml(e.description)}</span>
        <span class="expense-meta">Paid by ${escapeHtml(e.paidByName)} · ${date} · ${e.splitType === 'EQUAL' ? 'split equally' : 'exact split'}</span>
      </div>
      <span class="expense-amount">${money(e.amount)}</span>
    `;
    list.appendChild(row);
  });
}

function renderBalances(balanceResponse) {
  const balList = $('#balance-list');
  balList.innerHTML = '';
  balanceResponse.balances.forEach((b) => {
    const row = document.createElement('div');
    row.className = 'balance-row';
    const amt = Number(b.netAmount);
    const cls = amt > 0.005 ? 'credit' : amt < -0.005 ? 'debit' : 'even';
    const label = amt > 0.005 ? `+${money(amt)}` : amt < -0.005 ? `-${money(Math.abs(amt))}` : 'settled';
    row.innerHTML = `
      <span class="balance-name">${escapeHtml(b.name)}</span>
      <span class="balance-amount ${cls}">${label}</span>
    `;
    balList.appendChild(row);
  });

  const setList = $('#settlement-list');
  setList.innerHTML = '';
  if (balanceResponse.settlements.length === 0) {
    setList.innerHTML = '<p class="empty-row">Everyone is settled up. 🎉</p>';
    return;
  }
  balanceResponse.settlements.forEach((s) => {
    const row = document.createElement('div');
    row.className = 'settlement-row';
    row.innerHTML = `
      <span class="settlement-text"><span class="who">${escapeHtml(s.fromName)}</span> owes <span class="who">${escapeHtml(s.toName)}</span></span>
      <span class="settlement-amount">${money(s.amount)}</span>
    `;
    setList.appendChild(row);
  });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

// ---------- app shell wiring (modals triggers) ----------
function wireAppShell() {
  $('#new-group-btn').addEventListener('click', () => openModal('new-group-modal'));
  $('#add-member-btn').addEventListener('click', () => openModal('add-member-modal'));
  $('#add-expense-btn').addEventListener('click', () => {
    populateExpenseForm();
    openModal('add-expense-modal');
  });
}

// ---------- modals ----------
function wireModals() {
  $('#modal-backdrop').addEventListener('click', (e) => {
    if (e.target === $('#modal-backdrop')) closeModals();
  });
  $$('.modal-cancel').forEach((btn) => btn.addEventListener('click', closeModals));

  $('#new-group-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const name = $('#new-group-name').value.trim();
      const group = await Api.createGroup(name);
      state.groups.push(group);
      $('#new-group-name').value = '';
      closeModals();
      renderGroupList();
      selectGroup(group.id);
      showToast(`Created "${group.name}"`);
    } catch (err) {
      showToast(err.message, true);
    }
  });

  $('#add-member-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const email = $('#add-member-email').value.trim();
      const group = await Api.addMember(state.activeGroupId, email);
      state.activeGroup = group;
      $('#add-member-email').value = '';
      closeModals();
      renderGroupHeader(group);
      showToast('Member added');
    } catch (err) {
      showToast(err.message, true);
    }
  });

  $('#expense-split-type').addEventListener('change', populateExpenseForm);

  $('#add-expense-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const payload = buildExpensePayload();
      await Api.addExpense(state.activeGroupId, payload);
      closeModals();
      $('#add-expense-form').reset();
      showToast('Expense added');
      const [expenses, balances] = await Promise.all([
        Api.getExpenses(state.activeGroupId),
        Api.getBalances(state.activeGroupId),
      ]);
      renderExpenses(expenses);
      renderBalances(balances);
    } catch (err) {
      showToast(err.message, true);
    }
  });
}

function populateExpenseForm() {
  const group = state.activeGroup;
  if (!group) return;

  const paidBySelect = $('#expense-paid-by');
  paidBySelect.innerHTML = group.members
    .map((m) => `<option value="${m.id}">${escapeHtml(m.name)}</option>`)
    .join('');

  const splitType = $('#expense-split-type').value;
  const container = $('#expense-participants');
  container.innerHTML = '';

  group.members.forEach((m) => {
    const row = document.createElement('div');
    row.className = 'participant-row';
    row.innerHTML = `
      <label>
        <input type="checkbox" class="participant-check" value="${m.id}" checked>
        ${escapeHtml(m.name)}
      </label>
      ${splitType === 'EXACT'
        ? `<input type="number" step="0.01" min="0" class="participant-input" data-user="${m.id}" placeholder="0.00">`
        : ''}
    `;
    container.appendChild(row);
  });
}

function buildExpensePayload() {
  const description = $('#expense-description').value.trim();
  const amount = parseFloat($('#expense-amount').value);
  const paidByUserId = parseInt($('#expense-paid-by').value, 10);
  const splitType = $('#expense-split-type').value;

  const checked = $$('.participant-check').filter((c) => c.checked);
  if (checked.length === 0) {
    throw new Error('Select at least one participant to split with');
  }

  let splits;
  if (splitType === 'EQUAL') {
    splits = checked.map((c) => ({ userId: parseInt(c.value, 10) }));
  } else {
    splits = checked.map((c) => {
      const input = $(`.participant-input[data-user="${c.value}"]`);
      const splitAmount = parseFloat(input.value);
      if (isNaN(splitAmount)) {
        throw new Error('Enter an exact amount for every selected participant');
      }
      return { userId: parseInt(c.value, 10), amount: splitAmount };
    });
  }

  return { description, amount, paidByUserId, splitType, splits };
}