const host = process.env.RPC_HOST || 'localhost';
const port = process.env.RPC_PORT || 8545;

module.exports = {
  networks: {
    development: {
      host,
      port,
      network_id: "*", // Match any network id
      from: "0x00a329c0648769a73afac7f9381e08fb43dbea72"
    }
  }
};
