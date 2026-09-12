const API = (() => {
  const STORAGE_KEYS = {
    USERS: 'ledgerly_users',
    CURRENT_USER: 'ledgerly_current_user',
    GROUPS: 'ledgerly_groups'
  };

  function initStorage() {
    if (!localStorage.getItem(STORAGE_KEYS.USERS)) {
      const defaultUsers = [
        { id: 'u1', name: 'Thabo Mokoena', email: 'thabo@ledgerly.app', password: 'password123' },
        { id: 'u2', name: 'Aisha Naidoo', email: 'aisha@ledgerly.app', password: 'password123' },
        { id: 'u3', name: 'Kagiso Molefe', email: 'kagiso@ledgerly.app', password: 'password123' }
      ];
      localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(defaultUsers));
    }

    if (!localStorage.getItem(STORAGE_KEYS.GROUPS)) {
      const defaultGroups = [
        {
          id: 'g1',
          name: 'Durban Road Trip 🌴',
          createdBy: 'thabo@ledgerly.app',
          members: [
            { name: 'Thabo Mokoena', email: 'thabo@ledgerly.app' },
            { name: 'Aisha Naidoo', email: 'aisha@ledgerly.app' },
            { name: 'Kagiso Molefe', email: 'kagiso@ledgerly.app' }
          ],
          expenses: [
            {
              id: 'e1',
              description: 'Car Rental & Petrol',
              amount: 1500.00,
              paidBy: { name: 'Thabo Mokoena', email: 'thabo@ledgerly.app' },
              date: new Date(Date.now() - 86400000 * 3).toISOString(),
              splits: {
                'thabo@ledgerly.app': 500.00,
                'aisha@ledgerly.app': 500.00,
                'kagiso@ledgerly.app': 500.00
              }
            },
            {
              id: 'e2',
              description: 'Beachfront Dinner & Drinks',
              amount: 1260.00,
              paidBy: { name: 'Aisha Naidoo', email: 'aisha@ledgerly.app' },
              date: new Date(Date.now() - 86400000 * 2).toISOString(),
              splits: {
                'thabo@ledgerly.app': 420.00,
                'aisha@ledgerly.app': 420.00,
                'kagiso@ledgerly.app': 420.00
              }
            }
          ]
        }
      ];
      localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(defaultGroups));
    }
  }

  initStorage();

  const getUsers = () => JSON.parse(localStorage.getItem(STORAGE_KEYS.USERS)) || [];
  const setUsers = (users) => localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users));
  
  const getGroups = () => JSON.parse(localStorage.getItem(STORAGE_KEYS.GROUPS)) || [];
  const setGroups = (groups) => localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(groups));

  return {
    auth: {
      getSession() {
        const userJson = localStorage.getItem(STORAGE_KEYS.CURRENT_USER);
        return userJson ? JSON.parse(userJson) : null;
      },
      login(email, password) {
        const users = getUsers();
        const user = users.find(u => u.email.toLowerCase() === email.toLowerCase() && u.password === password);
        if (user) {
          const sessionUser = { id: user.id, name: user.name, email: user.email };
          localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(sessionUser));
          return sessionUser;
        }
        return null;
      },
      register(name, email, password) {
        const users = getUsers();
        if (users.some(u => u.email.toLowerCase() === email.toLowerCase())) {
          throw new Error('An account with this email already exists.');
        }
        const newUser = { id: 'u_' + Date.now(), name, email, password };
        users.push(newUser);
        setUsers(users);

        const sessionUser = { id: newUser.id, name: newUser.name, email: newUser.email };
        localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(sessionUser));
        return sessionUser;
      },
      logout() {
        localStorage.removeItem(STORAGE_KEYS.CURRENT_USER);
      }
    },

    groups: {
      listForUser(email) {
        const groups = getGroups();
        return groups.filter(g => g.members.some(m => m.email.toLowerCase() === email.toLowerCase()));
      },
      create(name, currentUser) {
        const groups = getGroups();
        const newGroup = {
          id: 'g_' + Date.now(),
          name,
          createdBy: currentUser.email,
          members: [
            { name: currentUser.name, email: currentUser.email }
          ],
          expenses: []
        };
        groups.push(newGroup);
        setGroups(groups);
        return newGroup;
      },
      addMember(groupId, email) {
        const users = getUsers();
        const userToAdd = users.find(u => u.email.toLowerCase() === email.toLowerCase());
        
        const memberName = userToAdd ? userToAdd.name : email.split('@')[0];

        const groups = getGroups();
        const group = groups.find(g => g.id === groupId);
        if (!group) throw new Error('Group not found.');

        if (group.members.some(m => m.email.toLowerCase() === email.toLowerCase())) {
          throw new Error('Member is already in this group.');
        }

        group.members.push({ name: memberName, email: email.toLowerCase() });
        setGroups(groups);
        return group;
      },
      addExpense(groupId, expenseData) {
        const groups = getGroups();
        const group = groups.find(g => g.id === groupId);
        if (!group) throw new Error('Group not found.');

        const users = getUsers();
        const payerUser = users.find(u => u.email.toLowerCase() === expenseData.paidByEmail.toLowerCase()) 
                          || group.members.find(m => m.email.toLowerCase() === expenseData.paidByEmail.toLowerCase());

        const newExpense = {
          id: 'e_' + Date.now(),
          description: expenseData.description,
          amount: parseFloat(expenseData.amount),
          paidBy: { name: payerUser ? payerUser.name : expenseData.paidByEmail, email: expenseData.paidByEmail },
          date: new Date().toISOString(),
          splits: expenseData.splits
        };

        group.expenses.push(newExpense);
        setGroups(groups);
        return group;
      }
    },

    calculateBalances(group) {
      const balances = {};
      group.members.forEach(m => {
        balances[m.email] = 0;
      });

      group.expenses.forEach(exp => {
        const payer = exp.paidBy.email;
        if (balances[payer] !== undefined) {
          balances[payer] += exp.amount;
        } else {
          balances[payer] = exp.amount;
        }

        Object.entries(exp.splits).forEach(([email, share]) => {
          if (balances[email] !== undefined) {
            balances[email] -= share;
          } else {
            balances[email] = -share;
          }
        });
      });

      return balances;
    },

    calculateSettlements(balances, members) {
      const debtors = [];
      const creditors = [];

      Object.entries(balances).forEach(([email, amount]) => {
        const memberObj = members.find(m => m.email === email);
        const name = memberObj ? memberObj.name : email;
        
        const rounded = Math.round(amount * 100) / 100;
        if (rounded < -0.01) {
          debtors.push({ email, name, amount: Math.abs(rounded) });
        } else if (rounded > 0.01) {
          creditors.push({ email, name, amount: rounded });
        }
      });

      debtors.sort((a, b) => b.amount - a.amount);
      creditors.sort((a, b) => b.amount - a.amount);

      const settlements = [];
      let i = 0, j = 0;

      while (i < debtors.length && j < creditors.length) {
        const debtor = debtors[i];
        const creditor = creditors[j];

        const settleAmount = Math.min(debtor.amount, creditor.amount);
        if (settleAmount > 0.01) {
          settlements.exports = true;
          settlements.push({
            fromEmail: debtor.email,
            fromName: debtor.name,
            toEmail: creditor.email,
            toName: creditor.name,
            amount: Math.round(settleAmount * 100) / 100
          });
        }

        debtor.amount -= settleAmount;
        creditor.amount -= settleAmount;

        if (debtor.amount < 0.01) i++;
        if (creditor.amount < 0.01) j++;
      }

      return settlements;
    }
  };
})();