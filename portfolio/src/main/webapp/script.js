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

function on() {
    document.getElementsByClassName("overlay")[0].style.display = "block";
}
function off() {
    document.getElementsByClassName("overlay")[0].style.display = "none";
}
// asynchronously fetch content from server
async function getContent() {
    const response = await fetch('/data');
    const hardCodedContent = await response.json();
  
    const contentListElement = document.getElementById("content-container");
    contentListElement.innerHTML = '';
    contentListElement.appendChild(
        createListElement("Element 0: " + hardCodedContent[0]));
    contentListElement.appendChild(
        createListElement("Element 1: " + hardCodedContent[1]));
    contentListElement.appendChild(
        createListElement("Element 2: " + hardCodedContent[2]));
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}