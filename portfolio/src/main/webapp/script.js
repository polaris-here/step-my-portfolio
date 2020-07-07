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
let map;
let editMarker;

function createMap() {
  console.log('createMap() exexuted');
  const locGoogleplex = {lat: 37.422, lng: -122.084};
  const labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const mapInit = {
    center: locGoogleplex,
    zoom: 16}
  // Initialize the map with <mapInit> values
  map = new google.maps.Map(
    document.getElementById('map'),
    mapInit);

  // Hardcode a marker onto the map
  const markerGoogleplex = new google.maps.Marker({
    position: locGoogleplex,
    map: map,
    title: 'Googleplex office'});

  // User clicks on map to pop up marker-creation-text-prompt
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  });
  // Get saved markers from datastore
  fetchMarkers();
}
// Helper: Fetch saved markers from database and adds them to map
async function fetchMarkers() {
  const response = await fetch('/markers');
  const markers = await response.json();

  markers.forEach(
    (marker) => {
      createMarkerForDisplay(marker.lat, marker.lng, marker.content)
    });
}
// Helper: Create markers, click on for pop-up info (text)
function createMarkerForDisplay(lat, lng, content) {
  const marker = 
    new google.maps.Marker({position: {lat: lat, lng: lng}, map: map});
  
  const infoWindow = new google.maps.InfoWindow({content: content});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}
// Helper: Store marker in database
function postMarker(lat, lng, content) {
  const params = new URLSearchParams();

  params.append('lat', lat);
  params.append('lng', lng);
  params.append('content', content);

  fetch('/markers', {method: 'POST', body: params});
}
/** Helper for on-click listen event: 
 *  Create marker with text-prompt
 */
function createMarkerForEdit(lat, lng) {
  // Remove marker if already showing another marker
  if (editMarker) {
    editMarker.setMap(null);
  }

  editMarker = 
    new google.maps.Marker({position: {lat: lat, lng: lng}, map: map});
  
  const infoWindow = 
    new google.maps.InfoWindow({content: buildInfoWindowInput(lat, lng)});
  
  // Remove marker if user closes editable info window
  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    editMarker.setMap(null);
  });

  infoWindow.open(map, editMarker);
}
/** Build and return HTML elements that show the editable textbox and submit
  * button
  */
function buildInfoWindowInput(lat, lng) {
  const textBox = document.createElement('textarea');
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Submit'));

  button.onclick = () => {
    postMarker(lat, lng, textBox.value);
    createMarkerForDisplay(lat, lng, textBox.value);
    editMarker.setMap(null);
  };

  const containerDiv = document.createElement('div');
  containerDiv.appendChild(textBox);
  containerDiv.appendChild(document.createElement('br'));
  containerDiv.appendChild(button);

  return containerDiv;
}
