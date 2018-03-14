# Distributed-Systems
Introduction
This application allows for multiple clients to connect to server and bid for items which are currently placed on the auction. This application is implemented using Java sockets and Java multi-threading. It is a simulation of online auction. I’ve modified chat server given to me by the lecturer to implement this assignment. 

Client Specification 

- Connects to the server. The item currently being offered for sale and the current bid or a (or reserve price) are displayed. 
-  Enter the bid. The amount entered should be greater than the current highest bid. 
- After a new bid is placed, the amount of the new bid must be displayed on the client’s window/console.  

Server Specification 

- Receive connections from multiple clients. 
- After a client connects, notify the client which item is currently on sale and the highest bid (or reserve price). 
- Specify the bid period. Max allowed 1 minute. When a new bid is raised, the bid period is reset back. 
- When a new bid is placed, all clients are notified immediately. Clients should be notified about the time left for bidding (when appropriate).
 - If the bid period elapses without a new bid, then the auction for this item closes. The successful bidder (if any) is chosen and all clients are notified.  
- When an auction for one item finishes, another item auctioning should start. Minimum of 5 items should be auctioned, one after another. Only one item at a time. 
- Any item not sold should be auctioned again (automatically).

Extra Functionality

-Sound during bidding process

-Informing auctioneers when last item on the auction is on the auction

-No one can bid anymore when auction is over

-Auction starts when there are two auctioneers connected to the server



