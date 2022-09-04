
/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{
	static int links = 0; //num of links we performed over all the program runtime
	static int cuts = 0; //num of cuts we performed over all the program runtime
	
	private HeapNode min; // the node with the minimal key in the heap
	private HeapNode first; // the first node in the heap
	private int n; // num of nodes in the heap
	private int numOfTrees; // num of trees in the heap
	private int numOfMarked; //num of marked nodes
	
	
	public FibonacciHeap() {} //default constructor (puts null/0 where needed)
	

   /**
    * public boolean isEmpty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    * complexity: O(1)  
    */
    public boolean isEmpty()
    {
    	return (this.n == 0);
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
    * complexity: O(1)  
    */
    public HeapNode insert(int key)
    { 
    	HeapNode node;
    	if(this.isEmpty())
    	{
    		node = new HeapNode(key, null , null , null); //create a new node, make it first
    		node.prev = node;
    		node.next = node;
    		this.min = node;
    	}
    	else
    	{   //add a new node to the beginning of the root list (make the node first):
    		node = new HeapNode(key, this.first.prev, this.first, null); 
    		this.first.prev = node;
    		node.prev.next = node;
    		if(key < this.min.key) //update min if necessary 
    		{
    			this.min = node;
    		}
    	}
    	//update fields:
    	this.first = node;
    	this.n = this.n + 1;
    	this.numOfTrees =this.numOfTrees + 1;
    	return node; 
    }

   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    * complexity: O(n)
    */
    public void deleteMin()
    {
    	if (!this.isEmpty()) {
    		HeapNode x = this.min;
    		if (x == x.next && x.child == null)  //min is the only node in the heap
    		{ 
    			//make the heap empty:
    			this.first = null;
    			this.min = null;
    			this.numOfTrees = 0;
    			this.numOfMarked = 0;
    		}
    		else if (x == x.next && x.child.next == x.child) //min is the only root in the heap and it has one child
    		{
    			//make the child of min the new min and first
    			this.first = x.child;
    			this.min = x.child;
    			x.child.mark = false;
    			x.child.parent = null;
    			this.numOfMarked = 0;
    		}
    		else //these are the cases we need to perform consolidating:
    		{
    			if (x.next == x && x.child.next != x.child) //min is the only root in the heap and it has more than one child
    			{
    				this.first = x.child; //make the sons of min to be the root list
    				makeSonsRoots(x);
    			}
    			else if (x != x.next) //min is not the only root in the heap
    			{
    				if(x.child != null) //min has at least one child
    				{
    					x.prev.next = x.child; //add sons of min to the root list in its place
	    				x.child.prev.next = x.next;
	    				x.next.prev = x.child.prev;
	    				x.child.prev = x.prev;
	    				if(this.first == x) //update the first pointer if min was first
	    				{
	    					this.first = x.child;
	    				}
	    				makeSonsRoots(x);
    				}
    				else //min doesn't have children
    				{
    					x.next.prev = x.prev; //remove min from the root list and update the minimum if necessary
						x.prev.next = x.next;
    					if (this.first == x) 
    					{
    						this.first = x.next;
    					}
    				}
    			}
    			this.min = x.next;
    			consolidate();
    		}
    		this.n --; //demote the number of nodes in the heap
    	}
    }
    
 
    /**
     * private void makeSonsRoots(HeapNode x) 
     *
     * make all sons of x to be roots by making their parent null
     * complexity: O(log n)
     */
    private void makeSonsRoots(HeapNode x) 
    {
    	x.child.parent = null; 
		HeapNode root = x.child.next;
		while(root != x.child) 
	   	{
			root.parent = null;
			if (root.isMarked()) {
				root.mark = false; //a root is never marked 
				this.numOfMarked --;
			}
			root = root.next;
	   	}
    }
    /**
     * private void consolidate()
     *
     * make the heap a binomial heap after deleting the node containing the minimum key,
     * and find the new minimum. 
     * (the method uses "buckets")
     * 
     * complexity: O(n)
     */
	private void consolidate() 
    {
    	double base = (1+Math.sqrt(5))/2; //the golden ratio
    	HeapNode[] buckets = new HeapNode[(int) (Math.log(n) / Math.log(base)) + 1]; //all buckets are null
    	this.first.prev.next = null;  //uncircle the root list
    	HeapNode z = this.first;
    	
    	//put trees into buckets corresponding to their rank, and link trees with the same rank:
    	while (z != null) 
    	{
    		HeapNode x = z;
    		int r = z.rank;
    		z = z.next; //save a pointer to the next root
    		while (buckets[r] != null) { //until we reach an empty bucket do:
    			HeapNode y = buckets[r]; //y is another node with the same rank as x
    			x = link(x,y);
    			buckets[r] = null; //empty the bucket
    			r++ ; //move the linked tree to the next bucket
    		}
    		buckets[r] = x; //the rank of x is now r, put x in that bucket
    	}
    	
    	//now we will rebuild the heap out of the buckets:
    	
    	this.min = null; //reset the heap
    	this.first = null;
    	this.numOfTrees = 0;
    	
    	for (int i=0; i<buckets.length; i++) {
    		HeapNode root = buckets[i];
    		if (root != null) //if there's a tree (root) in the bucket:
    		{
    			if (this.min == null) { //if the heap is empty, make the tree the first root and the minimum
        			this.first = root;
        			this.min = root;
        			root.prev = root;
        			root.next = root;
        		}
        		else {
        			insertAfter(root);
        			if (root.key < this.min.key) { ////update min if necessary
        				this.min = root;
        			}
        		}
        		this.numOfTrees++; //we added a tree to the heap
    		}
    	}
    }
	
    /**
     * private void insertAfter(HeapNode root)
     *
     * insert the tree rooted at 'root' to the heap, 
     * adding it to the right end of the root list.
     *
     * complexity: O(1)
     */
	private void insertAfter(HeapNode root) {
		root.prev = this.first.prev;
		this.first.prev.next = root;
		root.next = this.first;
		this.first.prev = root;
	}

	/**
     * private void link(HeapNode x, HeapNode y)
     *
     * unite two trees with the same rank, returning the new tree
     * complexity: O(1)
     */
    private HeapNode link(HeapNode x, HeapNode y)
    {
    	links++; //we make a link
    	
    	if(x.key > y.key) //make x to be the node with the smaller key:
    	{
    		HeapNode tmp = x;
    		x = y;
    		y = tmp;
    	}
    	//make x the parent of y:
    	if(x.rank > 0) //x and y are not single nodes
    	{
    		y.next = x.child;
    		y.parent = x;
    		y.prev = x.child.prev;
    		x.child.prev.next = y;
    		x.child.prev = y;	
    	}
    	else //x and y are single nodes
    	{
    		y.parent = x;
    		y.next = y;
    		y.prev = y;
    	}
    	//x is now the united new tree
    	x.rank++; //the rank is greater by 1 from the original rank of x and y
    	x.child = y; //make y the child of x
    	return x; 
    }

   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    * complexity: O(1)
    */
    public HeapNode findMin()
    {
    	return this.min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    * complexity: O(1)
    */
    public void meld (FibonacciHeap heap2)
    {
    	this.n = this.n + heap2.n;
		this.numOfMarked = this.numOfMarked + heap2.numOfMarked;
		this.numOfTrees = this.numOfTrees + heap2.numOfTrees;
		
    	if(this.isEmpty() && !heap2.isEmpty()) //if this is empty and the other isn't
    	{
    		this.min = heap2.min;
    		this.first = heap2.first;
    	}
    	else if(!this.isEmpty() && !heap2.isEmpty()) //if both heaps aren't empty
    	{
    		if(heap2.min.key < this.min.key) //update min to by the minimum out of the the two heaps
    		{
    			this.min = heap2.min;
    		}
    		
    		//meld the heaps by adding the root list of heap2 to the right end of the root list of this:
    		this.first.prev.next = heap2.first;
    		heap2.first.prev.next = this.first;
    		HeapNode tmp = heap2.first.prev;
    		heap2.first.prev = this.first.prev;
    		this.first.prev = tmp;
    	}
    	//for other cases (both heaps are empty, or this isn't empty and other is) - do nothing.    		
    }

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    * complexity: O(1) 
    */
    public int size()
    {
    	return this.n;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * complexity: O(n)
    */
    public int[] countersRep() 
    {
    	if(this.isEmpty())
    	{
    		return new int[] {};
    	}
    	
    	int maxRank = this.first.rank;
    	HeapNode node = this.first.next;
    	
    	//find the biggest rank in the root list:
    	while(node != this.first)
    	{
    		if(node.rank > maxRank)
    		{
    			maxRank = node.rank;
    		}
    		node = node.next;
    	}
	   int[] arr = new int[maxRank+1]; //initialize the counts array
       arr[first.rank] ++;
       node = first.next;
       
       //fill the array:
   	   while(node != this.first)
   	   {
   		   arr[node.rank] ++;
   		   node = node.next;
   	   }
	   return arr; 
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    * complexity: O(n)
    */
    public void delete(HeapNode x) 
    {   
    	if(this.n == 1) //there's only one node in the heap - make the heap empty
    	{
    		this.first = null;
    		this.numOfMarked = 0;
    		this.numOfTrees = 0;
    		this.min = null;
    		this.n = 0;
    	}
    	else
    	{
    		decreaseKey(x, x.key-this.min.key+1); //make the key of x to be the minimal key in the heap
    		this.deleteMin();
    	}
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    * complexity: O(n)
    */
    public void decreaseKey(HeapNode x, int delta)
    {    
    	assert (delta < 0) : "delta should be positive"; //increases x, but the method should decrease x
    	
    	if(delta > 0) //if delta == 0 we do nothing
    	{
    		x.key = x.key - delta; //decrease the key x by delta
    		if(x.key < this.min.key) //update min if necessary
			{
				this.min = x;
			}
    		HeapNode y = x.parent;
    		if( y != null && x.key < y.key) //min heap property is violated and x isn't a root
    		{
    			cut(x, y);
    			cascadingCuts(y);
    		}
    	}
    }
    
    /**
     * public void cut(HeapNode x, HeapNode y)
     *
     * The function cuts the connection between the node x and his parent y,
     * and adds x to the root list and 
     * complexity: O(1)
     */
    public void cut(HeapNode x, HeapNode y)
    {
    	cuts ++;  //we make a cut
    	y.rank --; //y lost a child
    	numOfTrees ++; //the tree rooted at x will be added to the root list
    	x.parent = null; //x is now a root
    	if (x.isMarked()) //unmark x if needed, roots are never marked
    	{
    		x.mark = false;
        	numOfMarked --;
    	}
    	
    	if(x.next == x)  //y.child pionter points at x, and x is the only child of y
    	{
    		y.child = null;
    	}
    	else if(y.child == x) //y.child pionter points at x, and x has brother/s
    	{
    		y.child = x.next;
    		x.prev.next = x.next;
    		x.next.prev = x.prev;
    	}
    	else //y.child pionter doesn't point at x, and x has brother/s
    	{
    		x.prev.next = x.next;
    		x.next.prev = x.prev;
    	}
    	
    	//insert x to the root list
    	x.next = this.first;
    	this.first.prev.next = x;
    	x.prev = this.first.prev;
    	this.first.prev = x;
    	this.first = x;
    }
    
    /**
     * public void cascadingCuts(HeapNode y)
     * 
     * @pre: a cut was performed on y's child.
     * recursive function
     * The function travels the nodes from y to the root. It marks y if it wasn't marked before.
     * If y was already marked, it cuts y and marks its parent.
     * The function stops when it marks a node that wasn't marked before.
     * 
     * complexity: O(n)
     */
    public void cascadingCuts(HeapNode y)
    {
    	HeapNode z = y.parent;
    	if(z != null) //y isn't a root
    	{
    		if(!y.isMarked())
    		{
    			y.mark = true;
    			numOfMarked ++;
    		}
    		else
    		{
    			cut(y, z);
    			cascadingCuts(z);
    		}
    	}
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * (the number of trees in the heap plus twice the number of marked nodes in the heap) 
    * complexity: O(1)
    */
    public int potential() 
    {    
    	return (this.numOfTrees + 2*this.numOfMarked);
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    * complexity: O(1)
    */
    public static int totalLinks()
    {    
    	return links;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    * complexity: O(1)
    */
    public static int totalCuts()
    {    
    	return cuts; 
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k minimal elements in a binomial tree H.
    * complexity: O(k(logk + deg(H))
    */
    
    public static int[] kMin(FibonacciHeap H, int k)
    {   
        FibonacciHeap helpHeap = new FibonacciHeap();
        int[] arr = new int[k];
    	if(H.isEmpty() || k == 0)
    	{
    		return arr;
    	}
        HeapNode child;
        HeapNode nextChild;
        HeapNode childInserted;
        arr[0] = H.min.key; //the root for H is with the small key for sure 
       
        //insert the first child of the root to the help heap
        child = H.min.child;
        childInserted = helpHeap.insert(child.key);
        childInserted.original = child;
       
         nextChild = child.next;
       
        //insert all the rest children of the root
        while (nextChild != child)
        {
        	 HeapNode nodeInserted = helpHeap.insert(nextChild.key);
        	 nodeInserted.original = nextChild;
        	 nextChild = nextChild.next;
        }
        for (int i = 1; i <= k-1; i++)
        {
        	HeapNode willBeDeleted = helpHeap.findMin();
        	arr[i] = willBeDeleted.key;
        	helpHeap.deleteMin();
        	HeapNode originalNode = willBeDeleted.original;
        	
        	//adding to the help heap the children of the node we deleted
        	child = originalNode.child;
            
        	if(child != null)
        	{
            	childInserted = helpHeap.insert(child.key);
                childInserted.original = child;
        		nextChild = child.next;
        		while (nextChild != child)
        	    {
        			HeapNode nodeInserted = helpHeap.insert(nextChild.key);
        	        nodeInserted.original = nextChild;
        	        nextChild = nextChild.next;
        	    }
        	}
        }
        return arr; 
    }
  
    
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode
    {
    	public int key; //the key of the node
    	private int rank; //the rank of the node
    	private boolean mark; //notes if the node is marked
    	private HeapNode child; //the child of the node
    	private HeapNode next; //the next node of this node
    	private HeapNode prev; //the previous node of this node
		private HeapNode parent; //the parent of the node
		private HeapNode original = null; //pointer the to original node (for Kmin function)
		
		  /**
		   * The method initialize an object of type HeapNode with values inserted
		   * complexity: O(1)
		   */
  		public HeapNode(int key, HeapNode prev, HeapNode next, HeapNode parent) 
  		{
  			this.key = key;
	    	this.child = null;
	    	this.rank = 0;
	    	this.mark = false;
	    	this.next = next;
	    	this.prev = prev;
	    	this.parent = parent;
  		}
  		
  	   /**
  	    * public int getKey()
  	    * 
  	    * the function returns the key of this node
  	    * complexity: O(1)
  	    */
  		public int getKey() 
  		{
  			return this.key;
  		}
  		
  	   /**
   	    * public void setKey(int key)
   	    * 
   	    * the function sets the key of this node
   	    * complexity: O(1)
   	    */
  		public void setKey(int key) 
  		{
  			this.key = key;
  		}
  		
   	   /**
   	    * public int getRank() 
   	    * 
   	    * the function returns the rank of this node
   	    * complexity: O(1)
   	    */
  		public int getRank() 
  		{
  			return this.rank;
  		}
  		
  		/**
    	* public void setRank(int rank)
        * 
        * the function sets the rank of this node
    	* complexity: O(1)
    	*/
  		public void setRank(int rank) 
  		{
  			this.rank = rank;
  		}
  		
  	    /**
    	* public boolean isMarked()
        * 
        * the function returns true if this node is marked and false if not
    	* complexity: O(1)
    	*/
  		public boolean isMarked() 
  		{
  			return this.mark;
  		}
  		
   	   /**
         * public HeapNode getPrev() 
    	 * 
    	 * the function returns the previous node of this node
    	 * complexity: O(1)
    	 */
  		public HeapNode getPrev() 
  		{
  			return this.prev;
  		}
  		
     /**
         * public void setPrev(HeapNode prev) 
    	 * 
    	 * the function sets the previous node of this node
    	 * complexity: O(1)
    	 */
  		public void setPrev(HeapNode prev) 
  		{
  			this.prev = prev;
  		}
  		
       /**
         * public HeapNode getNext()
    	 * 
    	 * the function returns the next node of this node
    	 * complexity: O(1)
    	 */
  		public HeapNode getNext() 
  		{
  			return this.next;
  		}
  		
        /**
         * public void setNext(HeapNode next)
    	 * 
    	 * the function sets the next node of this node
    	 * complexity: O(1)
    	 */
  		public void setNext(HeapNode next) 
  		{
  			this.next = next;
  		}
  		
        /**
         * public HeapNode getParent() 
    	 * 
    	 * the function returns the parent of this node
    	 * if there is no parent, returns null
    	 * complexity: O(1)
    	 */
  		public HeapNode getParent() 
  		{
  			return this.parent;
  		}
  		
        /**
         * public void setParent(HeapNode parent)  
    	 * 
    	 * the function sets the parent of this node
    	 * complexity: O(1)
    	 */
  		public void setParent(HeapNode parent) 
  		{
  			this.parent = parent;
  		}
  		
       /**
         * publicHeapNode getChild()
    	 * 
    	 * the function returns the left child of this node
    	 * if there is no child, returns null
    	 * complexity: O(1)
    	 */
  		public HeapNode getChild() 
  		{
  			return this.child;
  		}
  		
        /**
         * public void setChild()
    	 * 
    	 * the function sets the left child of this node 
    	 * complexity: O(1)
    	 */
  		public void setChild(HeapNode child) 
  		{
  			this.child = child;
  		}
  		

    }
}
