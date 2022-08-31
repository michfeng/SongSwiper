# Song Swiper

### [Final demo link](https://drive.google.com/file/d/1VVPmDG2Z83zKj_G80iLpSGb0H24cYIWR/view?usp=sharing) 

## Wireframes
![](https://i.imgur.com/4nomz5l.png)

## Schema
### Models
#### Post


| Property | Type     | Description |
| -------- | -------- | -------- |
| author | Pointer to User | post author |
| caption | String  | user-made caption |
| artpath | String | link to album art |
| likes | Relation of Users | maps relation of users who liked the post |
| uri | String | Spotify-specific track uri | 

#### Followers


| Property | Type     | Description |
| -------- | -------- | -------- |
| user | Pointer to User | user whose relationships are being tracked |
| followers | Relation of Users  | others who follow user |
| following | Relation of Users | friends that user follows |

#### LikedObjects


| Property | Type     | Description |
| -------- | -------- | -------- |
| user | Pointer to User | user whose likes are being tracked |
| likedTracks | Array  | liked tracks |
| likedGenres | Array | liked genres |
| likedArtists | Array | liked artists |

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

