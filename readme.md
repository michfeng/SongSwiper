# Song Swiper
1
## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Song recommendation app that reaches Spotify API and shares activity with friends


Ideas for functionality range, but I am hoping to have a list of potential features that I can work towards, with not all of them being necessarily "required." These are not fixed ideas yet, just things that come to mind:
- There used to be a feature on Spotify kept track of how fast/what pace you were running/walking at, and it would play music that had the same BPM
- **Music suggestions**
   - **"Tinder" but with new song suggestions: swipe left/right depending on whether you like a snippet of the song, adding "liked" songs to a playlist**
    - **Could have social-media potential (sharing what friends have recently "liked" on a feed, sharing playlists, connecting with people who have recently liked similar things)**
- Comparing music taste across general population or to other people who also connect their Spotify accounts
- Music-taste analysis
- Recommend upcoming concerts in the area (with maybe an adjustable range of distance/time)


## 
### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Music / social
- **Mobile:** Mobile aspect is necessary for convenience purposes (as with similar "sharing" social medias)
- **Story:** Helps people network/find common interests/discover new music
- **Market:** Any Spotify music user (which can be widened to anyone that listens to music at all), probably not podcast listeners, though
- **Habit:** Not necessarily meant to be a habit, but social/swiping aspects have historically been "hooks" on these kinds of apps
- **Scope:**

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can login/logout/register
* User can go through ("swipe through") recommendations
    * Snippets of songs play through swipes
* User can find "liked" songs in generated playlist
* User can share recently liked songs with friends
* User can follow others
* User can view others' recent songs/playlists (profiles)

**Optional Nice-to-have Stories**

* User can view music-taste analysis
* User can view upcoming concerts that are relevant to them, based on their music tastes
    * adjustable by time/distance
* User can view music comparison with friends

### 2. Screen Archetypes

* Login screen
    * User can login

* Registration screen
    * User can register for an account

* Swipe
* Post
* Search

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Swipe
* Search 
* Profile

## Wireframes
![](https://i.imgur.com/4nomz5l.png)

## Schema
### Models
#### Post


| Property | Type     | Description |
| -------- | -------- | -------- |
| objectId | String   | id for post (default field) |
| author | Pointer to User | post author |
| type | String  | type of post (for "liked" song vs. playlist) |
| createdAt | DateTime | date when post created (default field) |
| image | File | album or playlist art (depending on type) |
| artistImage | File | artist image (only for liked song type) | 

### Networking
#### List of network requests by screen

* Feed screen
    * (Read/GET) Query all posts in descending order by time
    * (Create/POST) Create new like on a post
    * (Delete) Delete existing like
    * (Create/POST) Create a new comment on a post
    * (Delete) Delete existing comment
    * (Create/POST) Add song to playlist (can be existing or not)
* Swipe screen
    * (Read/GET) Retrieve recommended songs
* "Liked" screen
    * (Create/POST) Create new post
* Profile screen
    * (Read/GET) Retrieve user data



#### Existing API endpoints
Base URL: https://api.spotify.com/v1


| HTTP Verb | Endpoint | Description |
| -------- | -------- | -------- |
| GET | /me/top/{type}     | gets current user's top artists/tracks (type is "artists" or "tracks" based on calculated affinity    |
| GET | /me | get profile information |
| GET | /recommendations | generated based on given "seed entity," which can be artists, generes, or tracks | 


Aaron was here!
