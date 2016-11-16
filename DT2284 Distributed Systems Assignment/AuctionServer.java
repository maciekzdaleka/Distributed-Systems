
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AuctionServer implements Runnable
{
	// Array of clients
	private AuctionServerThread clients[] = new AuctionServerThread[50];
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;
	private static final long TIMER1 = 60000; //Times for end of the biding and sounds
	private static final long TIMER2 = 30000;
	private static final long TIMER3 = 45000;
	private static final long TIMER4 = 45200;
	private static final long TIMER5 = 59600;
	private static final long TIMER6 = 59800;

	private ArrayList<Items> itemlist = new ArrayList<Items>(); //Array list to store array of items objects
	private String itemName; // local variable to store item name
	private int itemPrice; //local variable to store item price
	private static int  i = 0; //int used for object indexing
	private static Timer timer; // timer class for auction purposes
	private static int itemsCount = 5; //keeping the track of the amount of items
	private int current_client = 0; //very important int which determines if the bid was placed or not
	private int clientID; // int which stores client id
	private int start_auction = 0; // checking if 2 clients are connected so the auction can start

	public AuctionServer(int port)
	{
		try {
			System.out.println("Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);
			System.out.println("Server started: " + server.getInetAddress());
			start();
			addAuctionItems();
			addItemsToAuction(0);
		}
		catch(IOException ioe)
		{
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());

		}
	}

	public void run()
	{
		while (thread != null)
		{
			try{

				System.out.println("Waiting for a client ...");
				addThread(server.accept());

				int pause = (int)(Math.random()*3000);
				Thread.sleep(pause);

			}
			catch(IOException ioe){
				System.out.println("Server accept error: " + ioe);
				stop();
			}
			catch (InterruptedException e){
				System.out.println(e);
			}
		}
	}



	public void addAuctionItems() //function which add items object to the array when server starts running
	{
		itemlist.add(new Items("Iphone 6s", "20" ));
		itemlist.add(new Items("Samsung Galaxy s7", "20" ));
		itemlist.add(new Items("Mac Book Pro", "40" ));
		itemlist.add(new Items("Acer Aspire 5", "60" ));
		itemlist.add(new Items("Go Pro hero 3", "20" ));
	}
	public void addItemsToAuction(int n) //function which adds new items to the auction
	{
		itemName = itemlist.get(n).getName();
		itemPrice = Integer.decode(itemlist.get(n).getPrice());
	}

	public void start()
	{
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop(){
		thread = null;

	}

	private int findClient(int ID)
	{
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getID() == ID)
				return i;
		return -1;
	}

	public synchronized void broadcast(int ID, String input)
	{
		if (input.equals(".bye")) {
			clients[findClient(ID)].send(".bye");
			remove(ID);
		}
		else if(start_auction == 1) //checks if 2 clients are connected in order to start bidding
		{
			int clientbid = Integer.decode(input); //we are getting user input and converting into int
			if (clientbid > itemPrice && clientbid > 0 && clientbid < 1000000000 && itemsCount > 0) //checking if the bid is bigger than the item price
			{
				for (int i = 0; i < clientCount; i++)
				{
					if(clients[i].getID() != ID)
						System.out.println(ID + clientbid);
					clientID = ID;// getting the id of the client who placed the bid
				}
				itemPrice = clientbid;

				for(int i=0;i<clientCount; i++)
				{
					if(clients[i].getID() != clientID)
					{
						clients[i].send("\nHighest bid for:  " + itemName + " is: " + itemPrice + " Euro"); //sending info to auctioneers whats the current highest bid
					}
				}
				clients[findClient(ID)].send("\nYou have placed a new highest bid of: " + itemPrice + " Euro" + " for: " + itemName + " Euro"); //sends info to the bidder that he got the highest bid
				current_client = 1;
				timer.cancel();// reset the timer
				runTimer();//starts the timer again when new bid was placed
			}
			else if(itemsCount == 0)
			{
				clients[findClient(ID)].send("\nNo more bids allowed");
			}
			else //informs the client that the bid is not valid
			{
				clients[findClient(ID)].send("\nYou've placed either the same or lower bid for the " + itemName + " which is currently at the price of: " + itemPrice + " Euro");
			}
		}
		else
		{
			clients[findClient(ID)].send("\nPlease wait for the start of the auction...");
		}
		notifyAll();
	}
	public synchronized void remove(int ID)
	{
		int pos = findClient(ID);
		if (pos >= 0){
			AuctionServerThread toTerminate = clients[pos];
			System.out.println("Removing client thread " + ID + " at " + pos);

			if (pos < clientCount-1)
				for (int i = pos+1; i < clientCount; i++)
					clients[i-1] = clients[i];
			clientCount--;

			try{
				toTerminate.close();
			}
			catch(IOException ioe)
			{
				System.out.println("Error closing thread: " + ioe);
			}
			toTerminate = null;
			System.out.println("Client " + pos + " removed");
			notifyAll();
		}
	}
	public void welcome(int ID) // displays message after user connects to the server
	{
		if(clientCount == 2 && itemsCount > 0) // message displayed when 2nd auctioneer join the server and items are currently on sale
		{
			for(int i=0;i<clientCount; i++)
			{
				clients[i].send("\nWelcome to the auction. Current item for sale is:  " + itemName + " at the price: " + itemPrice + " Euro");
			}
			runTimer();//we start the auction
			start_auction =1;
		}
		else if(clientCount >=3 && itemsCount > 0)// message displayed when another auctioneers connect to server
		{												// the auction doesn't restarts
				clients[findClient(ID)].send("\nWelcome to the auction. Current item for sale is:  " + itemName + " at the price: " + itemPrice+ " Euro");
		}
		else if(clientCount == 1 && itemsCount > 0)// waiting for another client to connect to start the auction
		{
			clients[findClient(ID)].send("\nWelcome to the auction. Auction will start when we get another client! Please wait...");
			start_auction = 0;
		}
		else if(clientCount >=1 && itemsCount < 1)// when client connects but there is no more items for the auction
		{
				clients[findClient(ID)].send("\nAuction is over now. Please come again sometime soon");
		}
	}

	private void addThread(Socket socket)
	{

		if (clientCount < clients.length){

			System.out.println("Client accepted: " + socket);
			clients[clientCount] = new AuctionServerThread(this, socket);
			try{
				clients[clientCount].open();
				clients[clientCount].start();
				clientCount++;
				welcome(clients[clientCount-1].getID());//welcome message after client connects passes the id of the user that connects
			}
			catch(IOException ioe){
				System.out.println("Error opening thread: " + ioe);
			}
		}
		else
			System.out.println("Client refused: maximum " + clients.length + " reached.");
	}

	private void runTimer() //timer function where the whole auction logic developed
	{
		timer = new Timer("Countdown");
		timer.schedule(new TimerTask() {

			@Override
			public void run()
			{
				if(current_client == 0)// if this equals to 0 then nobody have placed the bid
				{
					itemlist.remove(i); // item is removed from the list
					itemlist.add(new Items(itemName,Integer.toString(itemPrice))); //the dame item is placed at the end of the array list
					addItemsToAuction(i);//since we removed the 1st item now the 2nd item is the 1st item
					if (itemsCount == 1) // if there is only one item the auctioneers are notified that it is the last item
					{
						for (int i = 0; i < clientCount; i++)
						{
							clients[i].send( " \n----------------------------------------------------------------------");
							clients[i].send("\nLast item for sale is:  " + itemName + "  at the price:  " + itemPrice+ " Euro");
						}
						timer.cancel();
						runTimer();
					}
					else if(itemsCount == 0) // in case something goes wrong we don't want to start the auction again
					{
						for (int i = 0; i < clientCount; i++)
						{
							clients[i].send("\n Please come again :)");
						}
					}
					else //if item wasn't sold we display the name and the price of the next item
					{
						for (int i = 0; i < clientCount; i++)
						{
							clients[i].send( " \n----------------------------------------------------------------------");
							clients[i].send("\nNext item for sale is:  " + itemName + "  at the price:  " + itemPrice + " Euro");
						}
						timer.cancel();// we cancel the timer and then start it again for 1 minute
						runTimer();
					}
				}
				else // if bid was placed
				{
					if(itemsCount == 2)//notifying auctioneers which item was sold, to who and for how much
					{
						Toolkit.getDefaultToolkit().beep();// 3rd beep noise notifying auctioneers that the
						clients[findClient(clientID)].send( " \nYou have won "+ itemName + " at the price:  " + itemPrice+ " Euro");//informing the user that he won the auction for the item
						for (int i = 0; i < clientCount; i++)
						{
							if(clients[i].getID() != clientID)
							{
								clients[i].send(" \n" + itemName + "  Was sold to " + clientID + " at the price:  " + itemPrice + " Euro");
							}
							clients[i].send( " \n----------------------------------------------------------------------");
						}
						itemlist.remove(i);//remove the item from the list
						addItemsToAuction(i);//adding the last item to the list
						itemsCount--;//deducting the item count
						for (int i = 0; i < clientCount; i++)
						{
							clients[i].send("\nLast item for sale is:  " + itemName + "  at the price:  " + itemPrice+ " Euro"); // notifying auctioneers that last item on sale in this auction
						}
						timer.cancel();//reset the timer again
						runTimer();
					}
					else if(itemsCount == 1)//
					{
						itemsCount --;//deducting the item count
						Toolkit.getDefaultToolkit().beep();
						clients[findClient(clientID)].send( " \nYou have won "+ itemName + " at the price:  " + itemPrice+ " Euro");
						for (int i = 0; i < clientCount; i++)
						{
							if(clients[i].getID() != clientID)
							{
								clients[i].send(" \n" + itemName + "  Was sold to " + clientID + " at the price:  " + itemPrice + " Euro");
							}
							clients[i].send( " \n----------------------------------------------------------------------");
						}
						for (int i = 0; i < clientCount; i++)
						{
							clients[i].send("\n------We have reached to end of the auction! Thank you very much for your participation------"); //message at the end of the auction
						}
						start_auction = 0;
						itemlist.remove(i);
						timer.cancel();// cancels the timer as the auction is over

					}
					else if(itemsCount == 0)// just for error handling. it shouldn't ever be displayed
					{
						for (int i = 0; i < clientCount; i++)
						{
							clients[i].send("\nAuction is over now :(");
						}
					}
					else//message send to auctioneers when item was sold
					{
						Toolkit.getDefaultToolkit().beep();
						clients[findClient(clientID)].send( " \nYou have won "+ itemName + " at the price:  " + itemPrice+ " Euro");
						for (int i = 0; i < clientCount; i++)
						{
							if(clients[i].getID() != clientID)
							{
								clients[i].send(" \n" + itemName + "  Was sold to " + clientID + " at the price:  " + itemPrice + " Euro");
							}
							clients[i].send( " \n----------------------------------------------------------------------");
						}
						itemlist.remove(i);//remove the item from the list
						addItemsToAuction(i);//adding the last item to the list
						itemsCount--;//deducting the item count
						for (int i = 0; i < clientCount; i++)
						{
							clients[i].send("\nNext item for sale is:  " + itemName + "  at the price:  " + itemPrice+ " Euro");
						}
						timer.cancel();
						runTimer();
					}
					current_client = 0;//setting the int to 0 so if no bid was placed for the next item then the item can be resold
				}
			}
		}, TIMER1);

		timer.schedule(new TimerTask() {

			@Override
			public void run()
			{
				for (int i = 0; i < clientCount; i++)
				{
					clients[i].send("\n30 seconds left for this auction!!!"); //reminding auctioneers how much left do they have to bid
				}
				if(current_client == 1) // beeps only when someone placed bid for the item
				{
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}, TIMER2);

		timer.schedule(new TimerTask() {

			@Override
			public void run()
			{
				for (int i = 0; i < clientCount; i++)
				{
					clients[i].send("\n15 seconds left for this auction!!!"); //reminding auctioneers how much left do they have to bid
				}
				if(current_client == 1) // beeps only when someone placed bid for the item
				{
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}, TIMER3);
		timer.schedule(new TimerTask() { //beeping at certain times to get the auction feel

			@Override
			public void run()
			{
				if(current_client == 1)
				{
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}, TIMER4);

		timer.schedule(new TimerTask() {

			@Override
			public void run()
			{
				if(current_client == 1)
				{
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}, TIMER5);

		timer.schedule(new TimerTask() {

			@Override
			public void run()
			{	if(current_client ==1)
			{
				Toolkit.getDefaultToolkit().beep();
			}
			}
		}, TIMER6);
	}



	public static void main(String args[]) {
		AuctionServer server = null;
		if (args.length != 1)
			System.out.println("Usage: java ChatServer port");
		else
			server = new AuctionServer(Integer.parseInt(args[0]));
	}


}
