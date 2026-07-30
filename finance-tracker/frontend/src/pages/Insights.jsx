import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchInsights, fetchPrediction } from '../redux/slices/insightsSlice';
import { currency } from '../utils/format';

export default function Insights() {
  const dispatch = useDispatch();
  const { insights, recommendations, provider, prediction, status, predictStatus } = useSelector((s) => s.insights);

  useEffect(() => {
    dispatch(fetchInsights());
    dispatch(fetchPrediction());
  }, [dispatch]);

  return (
    <div>
      <div className="page-eyebrow">AI-generated</div>
      <div className="page-header">
        <h1>Insights</h1>
        <button className="btn btn-outline" onClick={() => dispatch(fetchInsights())} disabled={status === 'loading'}>
          {status === 'loading' ? 'Refreshing…' : 'Refresh insights'}
        </button>
      </div>
      <hr className="rule" />

      <div className="grid grid-2">
        <div className="card">
          <div className="card-title">
            <span>What we noticed</span>
            {provider && (
              <span className={`insight-badge ${provider === 'openai' ? 'ai' : ''}`}>
                {provider === 'openai' ? 'AI-generated' : 'Heuristic engine'}
              </span>
            )}
          </div>

          {status === 'loading' && <div className="empty-state"><div className="spinner" style={{ margin: '0 auto' }} /></div>}

          {status !== 'loading' && (
            <ul className="insight-list">
              {insights.map((line, i) => <li key={i}>{line}</li>)}
            </ul>
          )}
        </div>

        <div className="card">
          <div className="card-title">Recommendations</div>
          <ul className="insight-list">
            {recommendations.map((line, i) => <li key={i}>{line}</li>)}
          </ul>
        </div>
      </div>

      <div className="card" style={{ marginTop: 20 }}>
        <div className="card-title">Spending prediction</div>
        {predictStatus === 'loading' && <div className="empty-state"><div className="spinner" style={{ margin: '0 auto' }} /></div>}
        {prediction && (
          <div className="grid grid-4">
            <div className="summary-tile">
              <div className="summary-label">Predicted expense</div>
              <div className="summary-value figure figure-expense">{currency(prediction.predictedExpense)}</div>
            </div>
            <div className="summary-tile">
              <div className="summary-label">Confidence</div>
              <div className="summary-value figure">{prediction.confidence}%</div>
            </div>
            <div className="summary-tile">
              <div className="summary-label">Budget risk</div>
              <div style={{ marginTop: 6 }}>
                <span className={prediction.budgetRisk ? 'badge-risk' : 'badge-ok'}>
                  {prediction.budgetRisk ? 'At risk of exceeding budget' : 'On track'}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
