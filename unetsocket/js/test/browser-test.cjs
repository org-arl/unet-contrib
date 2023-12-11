#!/usr/bin/env node

const process = require('process');

const puppeteer = require('puppeteer');
const statik = require('node-static');

console.log('\nSetting up local static server at http://localhost:8000/test');
const file = new statik.Server('.');
let server = require('http').createServer(function (request, response) {
  request.addListener('end', function () {
    file.serve(request, response);
  }).resume();
}).listen(8000);

if (process.argv.includes('-m')) {
  console.log('Waiting for manual test to start...');
} else {
  (async () => {
    console.log('Launching puppeteer..');
    const browser = await puppeteer.launch({ headless: 'new',});
    const page = await browser.newPage();
    page.on('console', msg => {
      msg.type() == 'error' && console.log('PAGE ERR:', msg.text());
      msg.type() == 'warning' && console.log('PAGE WARN:', msg.text());
    });
    await page.goto('http://localhost:8000/test', {waitUntil: 'networkidle2'});
    await page.waitForSelector('.jasmine-overall-result');
    const classList = await page.$eval('.jasmine-overall-result', (el) => el.classList);
    const classes = Object.values(classList);
    await page.waitForTimeout(100);
    await browser.close();
    console.log('Complete : ', classes.includes('jasmine-passed') ? 'PASSED':'FAILED');
    server.close();
  })();
}


