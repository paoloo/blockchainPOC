# bcpoc

BlochChain Proof-of-Concept using chain.com's developer edition blockchain and clojure


If you enjoy the concept or this is useful for you, consider buing a coffee to me ;D

[![Coffee](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://buymeacoff.ee/paoloo)

## Prerequisites

- You will need [Leiningen][] 2.0.0 or above installed;
- You will also need [Docker][]
- And love on your heart ;D

[leiningen]: https://github.com/technomancy/leiningen
[docker]: https://www.docker.com

## Running
To run everything together, just go with docker-compose:

	docker-compose up

Or, if you want to, run only the blockchain with docker

    docker run -p 1999:1999 chaincore/developer

get the CHAIN KEY from terminal, go http://localhost:1999, login and set everything up. Then, set the env var with the same CHAIN KEY, something like:

    export BCKEY=client:1db6ca297517df0524397018d5506afc28d48ac7977b0d2b64f2e81af1a48811

and, to start a web server for the application, run:

    lein ring server

Or run like heroku:

	lein with-profile production trampoline run

That's all ;D

## Deploy to Heroku
- Deploy chain to heroku first, follow instructions on
- Clone repository `git clone git@github.com:paoloo/bcpoc.git`
- Create heroku application `heroku create bcpoc`
- Add heroku endpoint `git remote add heroku https://git.heroku.com/bcpoc.git`
- Deploy! `git push heroku master`

## API Reference

- Create Wallet
  - **POST** /account
    - required JSON params: `username`: string
    - output: `id`: string - wallet id, `xpub`:string - wallet xpub
- Balance
  - **POST** /balance
    - required JSON params: `wallet`: string - wallet id from creation
    - output: `message`: int - balance value
- Issue Assets
  - **POST** /issue
    - required JSON params: `wallet`: string - wallet id, `amount`: int - amount to issue
    - output: `message`: string - response from node
- Transfer among wallets
  - **POST** /transfer
    - required JSON params: `origin`: string - origin wallet id, `originxpug`: string - origin wallet xpub, `destination`:string - destination wallet id, `amount`: int - amount to transfer
    - output: `message`: string - transaction status
- List transactions of the wallet
  - **POST** /list
    - required JSON params: `wallet`: string - wallet id from creation
    - output: `message`: array of transactions


## Examples
- Create wallets
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"username":"paolo"}' http://localhost:5000/account | jq '.'
```
```json
{
  "message": {
    "id": "acc1DHJ0XJ5G080A",
    "xpub": "e46a8ee3a91395998c3482c88ca8e67c663edd9c2442e477c093082e3248848fef1fdd5253003a51e8d323bf4e9bb559ee4f3eae1553b88c25067da49676846a"
  }
}
```
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"username":"sergio"}' http://localhost:5000/account | jq '.'
```
```json
{
  "message": {
    "id": "acc1DHJ13H60080C",
    "xpub": "48c1016c6fd094007376fe14397a1c38d68c0e9c7ac3212911f64e721c669baf787f41a41362c4f2fa464e29fb4d5b36aac0f131e6102d240a4127323649a7b5"
  }
}
```
- Check balance
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ0XJ5G080A"}' http://localhost:5000/balance | jq '.'
```
```json
{
  "message": 0
}
```
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ13H60080C"}' http://localhost:5000/balance | jq '.'
```
```json
{
  "message": 0
}
```
- Issue assets for a wallet
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ0XJ5G080A", "amount":20}' http://localhost:5000/issue | jq '.'
```
```json
{
  "message": "com.chain.api.Transaction$SubmitResponse@7eb71769"
}
```
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ0XJ5G080A"}' http://localhost:5000/balance | jq '.'
```
```json
{
  "message": 20
}
```
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ0XJ5G080A", "amount":90}' http://localhost:5000/issue | jq '.'
```
```json
{
  "message": "com.chain.api.Transaction$SubmitResponse@2517d79c"
}
```
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ0XJ5G080A"}' http://localhost:5000/balance | jq '.'
```
```json
{
  "message": 110
}
```
- Transfer from one wallet to another
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"origin":"acc1DHJ0XJ5G080A", "originxpub":"e46a8ee3a91395998c3482c88ca8e67c663edd9c2442e477c093082e3248848fef1fdd5253003a51e8d323bf4e9bb559ee4f3eae1553b88c25067da49676846a", "destination":"acc1DHJ13H60080C", "amount":"30"}' http://localhost:5000/transfer | jq '.'
```
```json
{
  "message": {
    "status": 0,
    "description": "Transaction successful."
  }
}
```
- Check the transaction asking for balance again
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ0XJ5G080A"}' http://localhost:5000/balance | jq '.'
```
```json
{
  "message": 80
}
```
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ13H60080C"}' http://localhost:5000/balance | jq '.'
```
```json
{
  "message": 30
}
```
- Listing all transactions
```bash
paolo@daath ~$ curl -s -XPOST -H 'Content-Type : application/json' -d '{"wallet":"acc1DHJ0XJ5G080A"}' http://localhost:5000/list | jq '.'
```
```json
{
  "message": [{"amount": 12, "asset": "PAOLOCOIN", "to": "Leticia", "type":"debit"}, {"amount": 50, "asset": "PAOLOCOIN", "to": "Sergio", "type":"debit"}]
}
```

## SDK Reference
- **JAVA**: https://chain.com/docs/1.2/java/javadoc/index.html
- **RUBY**: https://chain.com/docs/1.2/ruby/doc/index.html
- **NODE**: https://chain.com/docs/1.2/node/doc/index.html

## Docker
- Create a self contained version of application with: `lein ring uberjar`;

Then

- Run `docker build -t paoloo/bcpoc .` to create image;
- And finally, run `docker run -p 5000:5000 paoloo/bcpoc` to instantiate it.

OR

- docker-compose up

`and be happy ;D`

## ToDo
- **REFACTOR** as it is a copy-and-paste from REPL to test the concept
- create a middleware to tokenize wallet data(wallet id+xpub)
- create mobile-friendly frontend in clojurescript

## License
- Application

Copyright © 2018 Paolo Oliveira [MIT License](LICENSE)

- Chain.com's blockchain

Chain Core Developer Edition is licensed under the terms of the [GNU Affero General Public License Version 3 (AGPL)](https://github.com/chain/chain/blob/main/LICENSE).

The Chain Java SDK (`/sdk/java`) is licensed under the terms of the [Apache License Version 2.0](https://github.com/chain/chain/blob/main/sdk/java/LICENSE).
