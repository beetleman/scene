const MetaCoin = artifacts.require("./MetaCoin.sol");
const interval = process.env.INTERVAL || 1000;

const createAccountGenerator = () => {
  let accounts = [...web3.eth.accounts];
  accounts.reverse();
  return () => {
    const from = accounts.pop();
    const to = accounts.pop();
    accounts = [
      from,
      ...accounts,
      to
    ];
    return { from, to };
  };
};

const accountGenerator = createAccountGenerator();

const fireEvents = async (coin) => (setInterval(async () => {
  const amount = 42;
  const { from, to } = accountGenerator();
  console.log(`send ${amount} metacoin from ${from} to ${to}`);
  await coin.sendCoin(to, amount, { from });
}, interval));


module.exports = function(callback) {
  MetaCoin.deployed().then(fireEvents);
};
