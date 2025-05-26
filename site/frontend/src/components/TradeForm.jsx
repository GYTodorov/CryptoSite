import React, { useState } from 'react';
import api from '../Api';

function TradeForm({ refresh }) {
  const [pair, setPair] = useState('');
  const [quantity, setQuantity] = useState(0);
  const [message, setMessage] = useState('');

  const submit = (type) => {

    api.post(`/${type}`, { pair: pair, quantity })
      .then(res => {
        setMessage(res.data);
        refresh();
      })
      .catch(err => {
        if (err.response && err.response.data) {
          setMessage(err.response.data);  // This will show your "You can't buy..." message
        } else {
          setMessage('An error occurred');
        }
      });
  };

  return (
    <div>
      <h2>Trade</h2>
      <input
        placeholder="Pair (e.g., ETH/USD)"
        value={pair}
        onChange={e => setPair(e.target.value)}
      />
      <input
        type="number"
        value={quantity}
        onChange={e => setQuantity(Number(e.target.value))}
      />
      <button onClick={() => submit('buy')}>Buy</button>
      <button onClick={() => submit('sell')}>Sell</button>
      <p>{message}</p>
    </div>
  );
}

export default TradeForm;
