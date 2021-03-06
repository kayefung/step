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
      const commentContainer =
          createCommentContainer(message.nickname, message.text, message.score);

      document.getElementById('message-container')
          .appendChild(commentContainer);
    }
  });
}
getComments();

/**
 * Fetches login status data from /login and updates comment-form div in DOM
 *     depending on login status. Depending on status, DOM is updated to hide
 *     the form and prompt the user to login or out.
 */
function updateCommentForm() {
  fetch('/login')
  .then(response => response.json())
  .then((status) => {
    let commentForm = document.getElementById('comment-form');
    
    if (status.isLoggedIn) {
      // Display a comment form if the user is logged in.
      commentForm.appendChild(createCommentFormElement());
      
      let para = createLoginPromptElement(status.isLoggedIn, status.logUrl);
      commentForm.appendChild(para);
    }
    else {
      let para = createLoginPromptElement(status.isLoggedIn, status.logUrl);
      commentForm.appendChild(para);
    }
  });
}
updateCommentForm();

/**
 * Creates and returns a paragraph element that prompts the user to login/out
 *     and links to a URL.
 * @param {boolean} isLoggedIn A boolean that indicates whether the user is
 *     logged in or not.
 * @param {string} url A string of the URL for the user login/logout page.
 * @return {!HTMLParagraphElement} Paragraph element with text and anchor
 *     element that links to url.
 */
function createLoginPromptElement(isLoggedIn, url) {
  // Create anchor element that links to url
  const anchor = document.createElement('a');
  anchor.setAttribute('href', url);
  anchor.text = 'here';

  const para = document.createElement('p');

  // Append text nodes and anchor element as children of para
  if (isLoggedIn) {
    para.appendChild(document.createTextNode('Logout '));
    para.appendChild(anchor);
    para.appendChild(document.createTextNode('.'));
  }
  else {
    para.appendChild(document.createTextNode('Login '));
    para.appendChild(anchor);
    para.appendChild(document.createTextNode(' to comment.'));
  }

  return para;
}

/**
 * Creates and returns a form element for users to submit comments.
 * @return {!HTMLFormElement} Form element with text fields and submit button.
 */
function createCommentFormElement() {
  const form = document.createElement('form');
  form.setAttribute('action', '/data');
  form.setAttribute('method', 'POST');
  form.setAttribute('id', 'input-form');

  const nameLabel = document.createElement('label');
  nameLabel.setAttribute('for', 'nickname-input');
  nameLabel.appendChild(document.createTextNode('Name*'))

  const nameField = document.createElement('input');
  nameField.required = true;
  nameField.setAttribute('type', 'text');
  nameField.setAttribute('name', 'nickname-input');
  nameField.setAttribute('id', 'nickname-input');
  nameField.setAttribute('placeholder', 'Enter a display name.');

  const commentLabel = document.createElement('label');
  commentLabel.setAttribute('for', 'comment-input');
  commentLabel.appendChild(document.createTextNode('Comment*'))

  const commentField = document.createElement('input');
  commentField.required = true;
  commentField.setAttribute('type', 'text');
  commentField.setAttribute('name', 'comment-input');
  commentField.setAttribute('id', 'comment-input');
  commentField.setAttribute('placeholder', 'Write your comment here!');

  const submitButton = document.createElement('input');
  submitButton.setAttribute('type', 'submit');

  form.appendChild(nameLabel);
  form.appendChild(nameField);
  form.appendChild(commentLabel);
  form.appendChild(commentField);
  form.appendChild(submitButton);

  return form;
}

/**
 * Creates and returns a div element for a single comment.
 * @param {string} name A string of the commenter's display name.
 * @param {string} text A string of comment input. 
 * @param {double} score A double of the comment's sentiment score. 
 * @return {!HTMLDivElement} Div element that displays comment data. 
 */
function createCommentContainer(name, text, score) {
  // Create div that contains a comment's information.
  const commentContainer = document.createElement('div');
  commentContainer.className += 'comment-container';

  // Paragraph element that displays user's name and comment.
  const commentPara = document.createElement('p');
  commentPara.innerText = name + ': ' + text;
  commentContainer.appendChild(commentPara);

  // Paragraph element that displays sentiment score. 
  const sentimentPara = document.createElement('p');
  sentimentPara.className += 'sentiment';
  sentimentPara.innerText = 'Vibe: ' + score.toFixed(2);
  commentContainer.appendChild(sentimentPara);

  return commentContainer;
}