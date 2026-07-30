export default function SummaryCard({ label, value, tone }) {
  const toneClass = tone === 'income' ? 'figure-income' : tone === 'expense' ? 'figure-expense' : '';
  return (
    <div className="summary-tile">
      <div className="summary-label">{label}</div>
      <div className={`summary-value figure ${toneClass}`}>{value}</div>
    </div>
  );
}
