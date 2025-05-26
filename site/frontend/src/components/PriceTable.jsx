import React, { useEffect, useState } from 'react';
import api from '../Api';

function PriceTable() {
  const [prices, setPrices] = useState({});

  useEffect(() => {
    const fetchPrices = () => {
      api.get('/prices').then(res => setPrices(res.data));
    };
    fetchPrices();
    const interval = setInterval(fetchPrices, 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div>
      <h2>Live Prices</h2>
      <h3>Top 20 cryptocurrencies</h3>
      <table>
        <thead>
          <tr><th>Symbol</th><th>Price (USD)</th></tr>
        </thead>
        <tbody>
          {Object.entries(prices).map(([symbol, price]) => (
            <tr key={symbol}>
              <td>{symbol}</td>
              <td>${price.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default PriceTable;

