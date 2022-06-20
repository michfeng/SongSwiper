# Song Swiper


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

