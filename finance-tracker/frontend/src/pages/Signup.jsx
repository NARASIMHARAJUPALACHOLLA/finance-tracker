import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { signup, clearAuthError } from '../redux/slices/authSlice';

export default function Signup() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { status, error } = useSelector((state) => state.auth);
  const [form, setForm] = useState({ name: '', email: '', password: '' });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    dispatch(clearAuthError());
    const result = await dispatch(signup(form));
    if (signup.fulfilled.match(result)) navigate('/dashboard');
  };

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="auth-logo">
          <span className="stamp"><span className="stamp-mark">L</span></span>
          <span className="auth-logo-text">Ledger</span>
        </div>
        <h2>Open your ledger</h2>
        <p>Track income, expenses, and get AI-backed insight in minutes.</p>

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <label className="form-label">Name</label>
            <input className="form-input" type="text" name="name" required
                   value={form.name} onChange={handleChange} placeholder="Your name" />
          </div>
          <div className="form-row">
            <label className="form-label">Email</label>
            <input className="form-input" type="email" name="email" required
                   value={form.email} onChange={handleChange} placeholder="you@example.com" />
          </div>
          <div className="form-row">
            <label className="form-label">Password</label>
            <input className="form-input" type="password" name="password" required minLength={6}
                   value={form.password} onChange={handleChange} placeholder="At least 6 characters" />
          </div>

          {error && <div className="error-text">{error}</div>}

          <button className="btn btn-primary" type="submit" disabled={status === 'loading'} style={{ width: '100%', justifyContent: 'center' }}>
            {status === 'loading' ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <div className="auth-switch">
          Already have an account? <Link to="/login">Log in</Link>
        </div>
      </div>
    </div>
  );
}
