import React, { useEffect, useState } from 'react';
import api from '../Api';

function HistoryLog({ history }) {
  return (
    <div>
      <h2>Transaction History</h2>
      <table>
        <thead>
          <tr><th>Pair</th><th>Type</th><th>Qty</th><th>Price</th><th>Time</th><th>Profit/Loss</th></tr>
        </thead>
        <tbody>
          {history.map((tx, i) => (
            <tr key={i}>
              <td>{tx.pair}</td>
              <td>{tx.type}</td>
              <td>{tx.quantity}</td>
              <td>${parseFloat(tx.price).toFixed(2)}</td>
              <td>{new Date(tx.timestamp).toLocaleString()}</td>
              <td style={{ color: tx.profitLoss > 0 ? 'green' : tx.profitLoss < 0 ? 'red' : 'inherit'}}>
                {tx.profitLoss !== null && tx.profitLoss !== undefined
                  ? `$${tx.profitLoss.toFixed(2)}`
                  : "-"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default HistoryLog;
