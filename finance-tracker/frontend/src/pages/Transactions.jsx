import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchTransactions, createTransaction, updateTransaction, deleteTransaction
} from '../redux/slices/transactionSlice';
import { currency, CATEGORIES, PAYMENT_METHODS } from '../utils/format';

const emptyForm = {
  type: 'EXPENSE', title: '', amount: '', category: CATEGORIES[0],
  paymentMethod: PAYMENT_METHODS[0], description: '', transactionDate: new Date().toISOString().slice(0, 10)
};

export default function Transactions() {
  const dispatch = useDispatch();
  const { items, pagination, status } = useSelector((state) => state.transactions);

  const [filters, setFilters] = useState({ page: 1, limit: 10, category: '', type: '', search: '', sort: 'latest' });
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => { dispatch(fetchTransactions(filters)); }, [dispatch, filters]);

  const handleFilterChange = (e) => setFilters({ ...filters, [e.target.name]: e.target.value, page: 1 });
  const handleFormChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const openCreateForm = () => {
    setForm(emptyForm);
    setEditingId(null);
    setFormError('');
    setShowForm(true);
  };

  const openEditForm = (t) => {
    setForm({
      type: t.type, title: t.title, amount: t.amount, category: t.category,
      paymentMethod: t.paymentMethod || PAYMENT_METHODS[0], description: t.description || '',
      transactionDate: t.transactionDate
    });
    setEditingId(t.id);
    setFormError('');
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim() || !form.amount || Number(form.amount) <= 0 || !form.category.trim()) {
      setFormError('Please provide type, title, amount greater than zero and category.');
      return;
    }
    const payload = { ...form, amount: Number(form.amount) };
    const action = editingId
      ? updateTransaction({ id: editingId, payload })
      : createTransaction(payload);

    const result = await dispatch(action);
    if (result.error) {
      setFormError(result.payload || 'Something went wrong');
      return;
    }
    setShowForm(false);
    dispatch(fetchTransactions(filters));
  };

  const handleDelete = (id) => {
    if (window.confirm('Delete this transaction?')) dispatch(deleteTransaction(id));
  };

  return (
    <div>
      <div className="page-eyebrow">Ledger entries</div>
      <div className="page-header">
        <h1>Transactions</h1>
        <button className="btn btn-primary" onClick={openCreateForm}>+ Add transaction</button>
      </div>
      <hr className="rule" />

      <div className="toolbar">
        <input className="form-input" style={{ maxWidth: 220 }} name="search" placeholder="Search title, category…"
               value={filters.search} onChange={handleFilterChange} />
        <select className="form-select" style={{ maxWidth: 160 }} name="type" value={filters.type} onChange={handleFilterChange}>
          <option value="">All types</option>
          <option value="INCOME">Income</option>
          <option value="EXPENSE">Expense</option>
        </select>
        <select className="form-select" style={{ maxWidth: 180 }} name="category" value={filters.category} onChange={handleFilterChange}>
          <option value="">All categories</option>
          {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <select className="form-select" style={{ maxWidth: 180 }} name="sort" value={filters.sort} onChange={handleFilterChange}>
          <option value="latest">Latest first</option>
          <option value="oldest">Oldest first</option>
          <option value="amount_desc">Amount: high to low</option>
          <option value="amount_asc">Amount: low to high</option>
        </select>
      </div>

      {showForm && (
        <div className="card" style={{ marginBottom: 22 }}>
          <div className="card-title">{editingId ? 'Edit transaction' : 'New transaction'}</div>
          <form onSubmit={handleSubmit}>
            <div className="form-grid-2">
              <div className="form-row">
                <label className="form-label">Type</label>
                <select className="form-select" name="type" value={form.type} onChange={handleFormChange}>
                  <option value="EXPENSE">Expense</option>
                  <option value="INCOME">Income</option>
                </select>
              </div>
              <div className="form-row">
                <label className="form-label">Amount</label>
                <input className="form-input" type="number" min="0.01" step="0.01" name="amount"
                       value={form.amount} onChange={handleFormChange} />
              </div>
              <div className="form-row">
                <label className="form-label">Title</label>
                <input className="form-input" name="title" value={form.title} onChange={handleFormChange} />
              </div>
              <div className="form-row">
                <label className="form-label">Category</label>
                <select className="form-select" name="category" value={form.category} onChange={handleFormChange}>
                  {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
              <div className="form-row">
                <label className="form-label">Payment method</label>
                <select className="form-select" name="paymentMethod" value={form.paymentMethod} onChange={handleFormChange}>
                  {PAYMENT_METHODS.map((p) => <option key={p} value={p}>{p}</option>)}
                </select>
              </div>
              <div className="form-row">
                <label className="form-label">Date</label>
                <input className="form-input" type="date" name="transactionDate"
                       value={form.transactionDate} onChange={handleFormChange} />
              </div>
            </div>
            <div className="form-row">
              <label className="form-label">Description (optional)</label>
              <input className="form-input" name="description" value={form.description} onChange={handleFormChange} />
            </div>

            {formError && <div className="error-text">{formError}</div>}

            <div style={{ display: 'flex', gap: 10 }}>
              <button className="btn btn-primary" type="submit">{editingId ? 'Save changes' : 'Add transaction'}</button>
              <button className="btn btn-outline" type="button" onClick={() => setShowForm(false)}>Cancel</button>
            </div>
          </form>
        </div>
      )}

      <div className="card">
        {status === 'loading' && <div className="empty-state"><div className="spinner" style={{ margin: '0 auto' }} /></div>}

        {status !== 'loading' && items.length === 0 && (
          <div className="empty-state">No transactions match these filters yet.</div>
        )}

        {items.length > 0 && (
          <table className="ledger-table">
            <thead>
              <tr>
                <th>Date</th><th>Title</th><th>Category</th><th>Type</th>
                <th style={{ textAlign: 'right' }}>Amount</th><th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((t) => (
                <tr key={t.id}>
                  <td>{t.transactionDate}</td>
                  <td>{t.title}</td>
                  <td>{t.category}</td>
                  <td><span className={`pill ${t.type === 'INCOME' ? 'pill-income' : 'pill-expense'}`}>{t.type}</span></td>
                  <td style={{ textAlign: 'right' }}>
                    <span className={`figure ${t.type === 'INCOME' ? 'figure-income' : 'figure-expense'}`}>{currency(t.amount)}</span>
                  </td>
                  <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                    <button className="btn btn-outline" style={{ padding: '5px 10px', marginRight: 6 }} onClick={() => openEditForm(t)}>Edit</button>
                    <button className="btn btn-danger" style={{ padding: '5px 10px' }} onClick={() => handleDelete(t.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {pagination.pages > 1 && (
          <div className="toolbar" style={{ marginTop: 16, marginBottom: 0, justifyContent: 'flex-end' }}>
            <button className="btn btn-outline" disabled={filters.page <= 1}
                    onClick={() => setFilters({ ...filters, page: filters.page - 1 })}>Prev</button>
            <span style={{ fontSize: 13, color: 'var(--ink-soft)' }}>Page {pagination.page} of {pagination.pages}</span>
            <button className="btn btn-outline" disabled={filters.page >= pagination.pages}
                    onClick={() => setFilters({ ...filters, page: filters.page + 1 })}>Next</button>
          </div>
        )}
      </div>
    </div>
  );
}
