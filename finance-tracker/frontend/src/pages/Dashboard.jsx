import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, BarChart, Bar, Legend
} from 'recharts';
import { fetchDashboardSummary } from '../redux/slices/dashboardSlice';
import SummaryCard from '../components/SummaryCard';
import { currency } from '../utils/format';

const PIE_COLORS = ['#a1751f', '#9c3f2a', '#52705d', '#7c887f', '#c9a24a', '#b96a52', '#7d9683', '#c7b98f'];

export default function Dashboard() {
  const dispatch = useDispatch();
  const { summary, status } = useSelector((state) => state.dashboard);

  useEffect(() => { dispatch(fetchDashboardSummary()); }, [dispatch]);

  if (status === 'loading' && !summary) {
    return <div className="empty-state"><div className="spinner" style={{ margin: '0 auto' }} /></div>;
  }

  if (!summary) return null;

  return (
    <div>
      <div className="page-eyebrow">Overview</div>
      <div className="page-header">
        <h1>Dashboard</h1>
      </div>
      <hr className="rule" />

      <div className="grid grid-4" style={{ marginBottom: 24 }}>
        <SummaryCard label="Total Income" value={currency(summary.totalIncome)} tone="income" />
        <SummaryCard label="Total Expense" value={currency(summary.totalExpense)} tone="expense" />
        <SummaryCard label="Savings" value={currency(summary.savings)} tone={summary.savings >= 0 ? 'income' : 'expense'} />
        <SummaryCard label="Budget Remaining" value={currency(summary.budgetRemaining)} tone={summary.budgetRemaining >= 0 ? 'income' : 'expense'} />
      </div>

      <div className="grid grid-2" style={{ marginBottom: 24 }}>
        <div className="card">
          <div className="card-title">Monthly Trend</div>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={summary.monthlyTrend}>
              <CartesianGrid stroke="#ddd4bf" strokeDasharray="4 4" />
              <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#7c887f' }} />
              <YAxis tick={{ fontSize: 11, fill: '#7c887f' }} />
              <Tooltip formatter={(v) => currency(v)} />
              <Line type="monotone" dataKey="income" stroke="#a1751f" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="expense" stroke="#9c3f2a" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <div className="card-title">Category Breakdown</div>
          {summary.categoryBreakdown?.length ? (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={summary.categoryBreakdown} dataKey="amount" nameKey="category" outerRadius={80}>
                  {summary.categoryBreakdown.map((entry, i) => (
                    <Cell key={entry.category} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v) => currency(v)} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state">No expenses logged yet.</div>
          )}
        </div>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="card-title">Income vs Expense</div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={summary.monthlyTrend}>
              <CartesianGrid stroke="#ddd4bf" strokeDasharray="4 4" />
              <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#7c887f' }} />
              <YAxis tick={{ fontSize: 11, fill: '#7c887f' }} />
              <Tooltip formatter={(v) => currency(v)} />
              <Bar dataKey="income" fill="#a1751f" radius={[3, 3, 0, 0]} />
              <Bar dataKey="expense" fill="#9c3f2a" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <div className="card-title">Recent Activity</div>
          {summary.recentTransactions?.length ? (
            <table className="ledger-table">
              <tbody>
                {summary.recentTransactions.map((t) => (
                  <tr key={t.id}>
                    <td>
                      <div style={{ fontWeight: 500 }}>{t.title}</div>
                      <div style={{ fontSize: 12, color: 'var(--ink-faint)' }}>{t.category} · {t.transactionDate}</div>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <span className={`figure ${t.type === 'INCOME' ? 'figure-income' : 'figure-expense'}`}>
                        {t.type === 'INCOME' ? '+' : '-'}{currency(t.amount)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="empty-state">No transactions yet -- add your first one.</div>
          )}
        </div>
      </div>
    </div>
  );
}
