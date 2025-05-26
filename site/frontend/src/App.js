import React, { useState, useEffect } from 'react';
import api from './Api';
import PriceTable from './components/PriceTable';
import AccountPanel from './components/AccountPanel';
import TradeForm from './components/TradeForm';
import HistoryLog from './components/HistoryLog';

function App() {
  const [balance, setBalance] = React.useState(0);
  const [holdings, setHoldings] = React.useState([]);
  const [history, setHistory] = React.useState([]);

  const refresh = () => {
    api.get('/balance').then(res => setBalance(res.data));
    api.get('/holdings').then(res => setHoldings(res.data));
    api.get('/transactions').then(res => setHistory(res.data));
  };

  React.useEffect(() => {
    refresh();
  }, []);

  return (
    <div style={{ 
      maxWidth: '1200px', 
      margin: 'auto', 
      padding: '20px',
      display: 'flex',          // Enable flex layout
      flexDirection: 'column',  // Stack children vertically
    }}>
    <h1>🪙 Crypto Trading Simulator</h1>
    
    <div style={{
      display: 'flex',        // Nested flex container for columns
      gap: '20px',           // Space between columns
    }}>
    {/* Left Column */}
    <div style={{ flex: 1 }}> 
      <AccountPanel balance={balance} holdings={holdings} refresh={refresh} />
      <TradeForm refresh={refresh} />
      <HistoryLog history={history} />
    </div>
    
    {/* Right Column */}
    <div style={{ flex: 1 }}>
      <PriceTable />
    </div>
  </div>
</div>
  );
}

export default App;