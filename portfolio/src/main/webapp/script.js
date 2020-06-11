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
      ['I\'m a middle child!', 'I\'m a cat person!', 
      'One time I flushed a pair of sunglasses down a toilet:(', 
      'I can play the ukulele!'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Fetches messages from server and adds it to DOM
 */
function getComments() {
  const commentLimit = 15;

  let maxComments;
  try {
    maxComments = document.getElementById('max-comments').value;
    if (maxComments > commentLimit) {
        maxComments = commentLimit;
    }
  }
  catch (e) {
    // When you initially load page and haven't selected how many comments to
    // display, default to displaying 3 comments. 
    maxComments = 3;
  }

  const baseUrl = window.location.origin;
  let url = new URL('/data', baseUrl);
  url.searchParams.append('max-comments', maxComments);

  fetch(url) 
  .then(response => response.json())
  .then((messages) => {
    document.getElementById('message-container').innerHTML = '';

    for (message of messages) {
      const commentContainer = document.createElement('div');
      commentContainer.className += 'comment-container';
      commentContainer.innerText = message.email + ': ' + message.text;

      document.getElementById('message-container')
          .appendChild(commentContainer);
    }
  });
}
getComments();

/**
 * Checks login status of user, links to login/logout page, hides comment form
 * if user is logged out.
 */
function checkLoginStatus() {
  fetch('/login')
  .then(response => response.json())
  .then((status) => {
    const baseUrl = window.location.origin;
    let url = new URL(status[1], baseUrl);

    let commentForm = document.getElementById('comment-form');
    let para = document.createElement("p");
    
    if (status[0] === "True") {
      para.innerHTML = "<p>Logout <a href=\"" + url + "\">here</a>.</p>";
      commentForm.appendChild(para);
    }
    else {
      commentForm.getElementsByTagName('form')[0].style.display = "none";
      
      para.innerHTML = "<p>Login <a href=\"" + url +
          "\">here</a> to comment.</p>";
      commentForm.appendChild(para);
    }
  });
}
checkLoginStatus();