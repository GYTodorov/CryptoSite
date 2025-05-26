import React, { useEffect, useState } from 'react';
import api from '../Api';
import TradeForm from './TradeForm';

function AccountPanel({ balance, holdings, refresh }) {
  const handleReset = () => {
    api.post('/reset').then(refresh());
  };

  const formatNumber = (n) => (n != null ? n.toFixed(2) : 'N/A');

  return (
    <div>
      <h2>Account</h2>
      <p><strong>Balance:</strong> ${balance.toFixed(2)}</p>
      <button onClick={handleReset}>🔄 Reset Account</button>

      <h3>Holdings</h3>
      <table>
        <thead>
          <tr>
            <th>Pair</th>
            <th>Quantity</th>
            <th>Buy Price</th>
            <th>Bought Price</th>
            <th>Profit/Loss</th>
          </tr>
        </thead>
        <tbody>
          {holdings.map((h) => (
            <tr key={h.id}>
              <td>{h.pair}</td>
              <td>{h.quantity}</td>
              <td>{formatNumber(h.buyPrice)}</td>
              <td>{formatNumber(h.currentPrice)}</td>
              <td style={{ color: h.profitLoss > 0 ? 'green' : h.profitLoss < 0 ? 'red' : 'inherit' }}>
                {formatNumber(h.profitLoss)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AccountPanel;

