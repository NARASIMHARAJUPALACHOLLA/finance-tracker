export const currency = (value) => {
  const n = Number(value || 0);
  return `\u20b9${Math.round(n).toLocaleString('en-IN')}`;
};

export const CATEGORIES = [
  'Food', 'Rent', 'Transport', 'Entertainment', 'Utilities',
  'Healthcare', 'Shopping', 'Education', 'Salary', 'Freelance', 'Other'
];

export const PAYMENT_METHODS = ['Cash', 'Card', 'UPI', 'Bank Transfer', 'Other'];
