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
 * Randomly selects a background color when page refreshes.
 */
function setBackgroundColor() {
  const bgColors = ['#1A535C', '#4ECDC4', '#F7FFF7', '#A82431', '#FFE66D'];

  // Pick a random hex code from bgColors
  const bgColor = bgColors[Math.floor(Math.random() * bgColors.length)];
  
  // Change the background color of page to bgColor.
  document.body.style.backgroundColor = bgColor;

  // Adjust text color depending on background color selected.
  setTextColor(bgColor);
}

/**
 * Modifies the color of text depending on the background color, for visibility. 
 * Uses W3 color contrast standard for calculations https://www.w3.org/TR/AERT/#color-contrast
 */
function setTextColor(bgColor) {
  // Converts hex code to rgb values.
  let red = parseInt(bgColor[1] + bgColor[2], 16);
  let green = parseInt(bgColor[3] + bgColor[4], 16);
  let blue = parseInt(bgColor[5] + bgColor[6], 16);

  // Calculates the color difference using a W3 formula.
  let colorDifference = (Math.max(255, red) - Math.min(255, red)) 
    + (Math.max(255, green) - Math.min(255, green)) 
    + (Math.max(255, blue) - Math.min(255, blue));

  // Sets text color to white if color difference is out of range. An out of
  // range color difference means the two colors have good visibility.
  if (colorDifference > 500) {
    document.body.style.color = '#FFFFFF';
  }
  else {
    document.body.style.color = '#000000';
  }
}

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
