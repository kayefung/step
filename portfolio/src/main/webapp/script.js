// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Randomly selects a background color when page refreshes and adjusts
 * text color accordingly, for visibility.
 */
const setPageColors = () => {
  const bgColors = ['#1A535C', '#4ECDC4', '#F7FFF7', '#A82431', '#FFE66D'];
  const fgColors = ['#FFFFFF', '#000000', '#000000', '#FFFFFF', '#000000'];

  // Pick a random pair of background and text colors.
  const colorPair = Math.floor(Math.random() * bgColors.length);
  
  // Change the background color of page to bgColor.
  document.body.style.backgroundColor = bgColors[colorPair];

  // Change text color corresponding to background color.
  document.body.style.color = fgColors[colorPair];
};
window.onload = setPageColors; 

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['I\'m a middle child!', 'I\'m a cat person!', 'One time I flushed a pair of sunglasses down a toilet:(',
        'I can play the ukulele!'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Fetches message from server and adds it to DOM
 */
function getMessage() {
  fetch('/data')
  .then(response => response.text())
  .then((message) => {
    // innerHTML is safe in this case because message comes from a static 
    // String in the server. 
    document.getElementById('message-container').innerHTML = message;
  });
}