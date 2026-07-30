import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import api from '../api/axios';
import SummaryCard from '../components/SummaryCard';
import { currency } from '../utils/format';

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

export default function Reports() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState('');

  const loadReport = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/reports/monthly', { params: { year, month } });
      setReport(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load report');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadReport(); /* eslint-disable-next-line */ }, [year, month]);

  const handleExportPdf = async () => {
    setExporting(true);
    try {
      const res = await api.get('/reports/monthly', {
        params: { year, month, format: 'pdf' },
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.download = `finance-report-${year}-${String(month).padStart(2, '0')}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Failed to export PDF');
    } finally {
      setExporting(false);
    }
  };

  const years = Array.from({ length: 6 }, (_, i) => now.getFullYear() - i);

  return (
    <div>
      <div className="page-eyebrow">Monthly summary</div>
      <div className="page-header">
        <h1>Reports</h1>
        <button className="btn btn-primary" onClick={handleExportPdf} disabled={exporting || !report}>
          {exporting ? 'Exporting…' : 'Export PDF'}
        </button>
      </div>
      <hr className="rule" />

      <div className="toolbar">
        <select className="form-select" style={{ maxWidth: 160 }} value={month} onChange={(e) => setMonth(Number(e.target.value))}>
          {MONTH_NAMES.map((m, i) => <option key={m} value={i + 1}>{m}</option>)}
        </select>
        <select className="form-select" style={{ maxWidth: 120 }} value={year} onChange={(e) => setYear(Number(e.target.value))}>
          {years.map((y) => <option key={y} value={y}>{y}</option>)}
        </select>
      </div>

      {error && <div className="error-text">{error}</div>}
      {loading && <div className="empty-state"><div className="spinner" style={{ margin: '0 auto' }} /></div>}

      {!loading && report && (
        <>
          <div className="grid grid-4" style={{ marginBottom: 24 }}>
            <SummaryCard label="Total Income" value={currency(report.totalIncome)} tone="income" />
            <SummaryCard label="Total Expense" value={currency(report.totalExpense)} tone="expense" />
            <SummaryCard label="Savings" value={currency(report.savings)} tone={report.savings >= 0 ? 'income' : 'expense'} />
            <SummaryCard label="Budget Remaining" value={currency(report.budgetRemaining)} tone={report.budgetRemaining >= 0 ? 'income' : 'expense'} />
          </div>

          <div className="card" style={{ marginBottom: 24 }}>
            <div className="card-title">Category Amounts</div>
            {report.categoryBreakdown?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={report.categoryBreakdown}>
                  <CartesianGrid stroke="#ddd4bf" strokeDasharray="4 4" />
                  <XAxis dataKey="category" tick={{ fontSize: 11, fill: '#7c887f' }} />
                  <YAxis tick={{ fontSize: 11, fill: '#7c887f' }} />
                  <Tooltip formatter={(v) => currency(v)} />
                  <Bar dataKey="amount" fill="#a1751f" radius={[3, 3, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="empty-state">No expenses recorded for this period.</div>
            )}
          </div>

          <div className="card">
            <div className="card-title">Transactions this period</div>
            {report.transactions?.length ? (
              <table className="ledger-table">
                <thead>
                  <tr><th>Date</th><th>Title</th><th>Category</th><th>Type</th><th style={{ textAlign: 'right' }}>Amount</th></tr>
                </thead>
                <tbody>
                  {report.transactions.map((t) => (
                    <tr key={t.id}>
                      <td>{t.transactionDate}</td>
                      <td>{t.title}</td>
                      <td>{t.category}</td>
                      <td><span className={`pill ${t.type === 'INCOME' ? 'pill-income' : 'pill-expense'}`}>{t.type}</span></td>
                      <td style={{ textAlign: 'right' }}>
                        <span className={`figure ${t.type === 'INCOME' ? 'figure-income' : 'figure-expense'}`}>{currency(t.amount)}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="empty-state">No transactions for this period.</div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
