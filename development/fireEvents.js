const MetaCoin = artifacts.require("./MetaCoin.sol");
const interval = process.env.INTERVAL || 1000;

const createAccountGenerator = () => {
  const from  = '0x00a329c0648769a73afac7f9381e08fb43dbea72';
  const length = from.length;
  const maxAddress = web3.toDecimal(
    '0x' + 'f'.repeat(length - 2)
  );
  return () => {
    const to = web3.toHex(
      Math.random() * maxAddress
    ).padEnd(
      length, 0
    );
    return { from, to };
  };
};


const accountGenerator = createAccountGenerator();

const fireEvents = async (coin) => (setInterval(async () => {
  const amount = Math.floor(Math.random() * 1000);
  const { from, to } = accountGenerator();
  console.log(`send ${amount} metacoin from ${from} to ${to}`);
  await coin.sendCoin(to, amount, { from });
}, interval));


module.exports = function(callback) {
  MetaCoin.deployed().then(fireEvents);
};
