import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchBudget, saveBudget } from '../redux/slices/budgetSlice';
import { fetchDashboardSummary } from '../redux/slices/dashboardSlice';
import { currency, CATEGORIES } from '../utils/format';

export default function Budget() {
  const dispatch = useDispatch();
  const { monthlyBudget, categoryBudgets, status } = useSelector((state) => state.budget);
  const { summary } = useSelector((state) => state.dashboard);

  const [monthly, setMonthly] = useState(0);
  const [rows, setRows] = useState([]);

  useEffect(() => {
    dispatch(fetchBudget());
    dispatch(fetchDashboardSummary());
  }, [dispatch]);

  useEffect(() => {
    setMonthly(monthlyBudget || 0);
    setRows(categoryBudgets.length ? categoryBudgets : [{ category: CATEGORIES[0], limit: '' }]);
  }, [monthlyBudget, categoryBudgets]);

  const spentByCategory = (category) => {
    const entry = summary?.categoryBreakdown?.find((c) => c.category === category);
    return entry ? Number(entry.amount) : 0;
  };

  const totalExpense = summary ? Number(summary.totalExpense) : 0;
  const overBudget = monthly > 0 && totalExpense > monthly;

  const updateRow = (idx, field, value) => {
    const next = [...rows];
    next[idx] = { ...next[idx], [field]: value };
    setRows(next);
  };

  const addRow = () => setRows([...rows, { category: CATEGORIES[0], limit: '' }]);
  const removeRow = (idx) => setRows(rows.filter((_, i) => i !== idx));

  const handleSave = async (e) => {
    e.preventDefault();
    const payload = {
      monthlyBudget: Number(monthly) || 0,
      categoryBudgets: rows
        .filter((r) => r.category && r.limit !== '')
        .map((r) => ({ category: r.category, limit: Number(r.limit) }))
    };
    await dispatch(saveBudget(payload));
    dispatch(fetchDashboardSummary());
  };

  return (
    <div>
      <div className="page-eyebrow">Spending limits</div>
      <div className="page-header"><h1>Budget</h1></div>
      <hr className="rule" />

      {overBudget && (
        <div className="card" style={{ marginBottom: 20, borderColor: 'var(--rust)', background: 'var(--rust-soft)' }}>
          <strong style={{ color: 'var(--rust)' }}>Overspending alert:</strong>{' '}
          You've spent {currency(totalExpense)} against a {currency(monthly)} monthly budget.
        </div>
      )}

      <div className="grid grid-2">
        <div className="card">
          <div className="card-title">Monthly budget</div>
          <form onSubmit={handleSave}>
            <div className="form-row">
              <label className="form-label">Total monthly limit</label>
              <input className="form-input" type="number" min="0" step="1"
                     value={monthly} onChange={(e) => setMonthly(e.target.value)} />
            </div>

            <div className="card-title" style={{ marginTop: 20 }}>Category limits</div>
            {rows.map((row, idx) => (
              <div key={idx} className="form-grid-2" style={{ marginBottom: 10 }}>
                <select className="form-select" value={row.category}
                        onChange={(e) => updateRow(idx, 'category', e.target.value)}>
                  {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
                </select>
                <div style={{ display: 'flex', gap: 8 }}>
                  <input className="form-input" type="number" min="0" placeholder="Limit"
                         value={row.limit} onChange={(e) => updateRow(idx, 'limit', e.target.value)} />
                  <button type="button" className="btn btn-outline" onClick={() => removeRow(idx)}>×</button>
                </div>
              </div>
            ))}
            <button type="button" className="btn btn-outline" onClick={addRow} style={{ marginBottom: 18 }}>+ Add category limit</button>

            <div>
              <button className="btn btn-primary" type="submit" disabled={status === 'loading'}>Save budget</button>
            </div>
          </form>
        </div>

        <div className="card">
          <div className="card-title">Category progress</div>
          {rows.filter((r) => r.limit).length === 0 && (
            <div className="empty-state">Set category limits to see progress bars here.</div>
          )}
          {rows.filter((r) => r.limit).map((row) => {
            const spent = spentByCategory(row.category);
            const limit = Number(row.limit) || 0;
            const pct = limit > 0 ? Math.min(100, Math.round((spent / limit) * 100)) : 0;
            const isOver = limit > 0 && spent > limit;
            return (
              <div key={row.category} style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6 }}>
                  <span>{row.category}</span>
                  <span className="figure">{currency(spent)} / {currency(limit)}</span>
                </div>
                <div className="progress-track">
                  <div className={`progress-fill${isOver ? ' over' : ''}`} style={{ width: `${pct}%` }} />
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
