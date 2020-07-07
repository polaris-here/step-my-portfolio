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

// Functions for css effects
function on() {
  document.getElementsByClassName("overlay")[0].style.display = "block";
}
function off() {
  document.getElementsByClassName("overlay")[0].style.display = "none";
}

// Functions for server
// asynchronously fetch content from server
async function getComments(value=2) {
  const response = await fetch('/data?comment-limit-choice=' + value);
  const content = await response.json();
  
  const contentListElement = document.getElementById("comment-container");
  contentListElement.innerHTML = '';
  for(let i = 0; i < content.length; i++) {
    contentListElement.appendChild(
      createListElement(content[i]))
  }
}
/** Helper func: Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
// Delete comments
async function deleteData() {
  const request = new Request('/delete-data', {method: 'POST'});
  await fetch(request);
  
  // Remove now-deleted comments from page
  getComments();
}

// Functions for Google Maps
// Make the map
function createMap() {
  console.log("createMap() exexuted");
  const locGoogleplex = {lat: 37.422, lng: -122.084};
  const mapInit = {
    center: locGoogleplex,
    zoom: 16}

  const map = new google.maps.Map(
    document.getElementById("map"),
    mapInit);
  console.log("createMap() finished");

  const markerGoogleplex = new google.maps.Marker({
    position: locGoogleplex,
    map: map,
    title: 'Googleplex office'});
}
