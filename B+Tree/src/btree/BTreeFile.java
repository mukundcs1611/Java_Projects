/*
 * @(#) bt.java   98/03/24
 * Copyright (c) 1998 UW.  All Rights Reserved.
 *         Author: Xiaohu Li (xioahu@cs.wisc.edu).
 *
 */

package btree;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import bufmgr.BufMgrException;
import bufmgr.BufferPoolExceededException;
import bufmgr.HashEntryNotFoundException;
import bufmgr.HashOperationException;
import bufmgr.InvalidFrameNumberException;
import bufmgr.PageNotReadException;
import bufmgr.PagePinnedException;
import bufmgr.PageUnpinnedException;
import bufmgr.ReplacerException;
import diskmgr.Page;
import global.AttrType;
import global.GlobalConst;
import global.PageId;
import global.RID;
import global.SystemDefs;
import heap.HFPage;
/**
 * btfile.java This is the main definition of class BTreeFile, which derives
 * from abstract base class IndexFile. It provides an insert/delete interface.
 */
public class BTreeFile extends IndexFile implements GlobalConst {

	private final static int MAGIC0 = 1989;

	private final static String lineSep = System.getProperty("line.separator");

	private static FileOutputStream fos;
	private static DataOutputStream trace;

	/**
	 * It causes a structured trace to be written to a file. This output is used
	 * to drive a visualization tool that shows the inner workings of the b-tree
	 * during its operations.
	 *
	 * @param filename
	 *            input parameter. The trace file name
	 * @exception IOException
	 *                error from the lower layer
	 */
	public static void traceFilename(String filename) throws IOException {

		fos = new FileOutputStream(filename);
		trace = new DataOutputStream(fos);
	}

	/**
	 * Stop tracing. And close trace file.
	 *
	 * @exception IOException
	 *                error from the lower layer
	 */
	public static void destroyTrace() throws IOException {
		if (trace != null)
			trace.close();
		if (fos != null)
			fos.close();
		fos = null;
		trace = null;
	}

	private BTreeHeaderPage headerPage;
	private PageId headerPageId;
	private String dbname;

	/**
	 * Access method to data member.
	 * 
	 * @return Return a BTreeHeaderPage object that is the header page of this
	 *         btree file.
	 */
	public BTreeHeaderPage getHeaderPage() {
		return headerPage;
	}

	private PageId get_file_entry(String filename) throws GetFileEntryException {
		try {
			return SystemDefs.JavabaseDB.get_file_entry(filename);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GetFileEntryException(e, "");
		}
	}

	private Page pinPage(PageId pageno) throws PinPageException {
		try {
			Page page = new Page();
			SystemDefs.JavabaseBM.pinPage(pageno, page, false/* Rdisk */);
			return page;
		} catch (Exception e) {
			e.printStackTrace();
			throw new PinPageException(e, "");
		}
	}

	private void add_file_entry(String fileName, PageId pageno)
			throws AddFileEntryException {
		try {
			SystemDefs.JavabaseDB.add_file_entry(fileName, pageno);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AddFileEntryException(e, "");
		}
	}

	private void unpinPage(PageId pageno) throws UnpinPageException {
		try {
			SystemDefs.JavabaseBM.unpinPage(pageno, false /* = not DIRTY */);
		} catch (Exception e) {
			e.printStackTrace();
			throw new UnpinPageException(e, "");
		}
	}

	private void freePage(PageId pageno) throws FreePageException {
		try {
			SystemDefs.JavabaseBM.freePage(pageno);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FreePageException(e, "");
		}

	}

	private void delete_file_entry(String filename)
			throws DeleteFileEntryException {
		try {
			SystemDefs.JavabaseDB.delete_file_entry(filename);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeleteFileEntryException(e, "");
		}
	}

	private void unpinPage(PageId pageno, boolean dirty)
			throws UnpinPageException {
		try {
			SystemDefs.JavabaseBM.unpinPage(pageno, dirty);
		} catch (Exception e) {
			e.printStackTrace();
			throw new UnpinPageException(e, "");
		}
	}

	/**
	 * BTreeFile class an index file with given filename should already exist;
	 * this opens it.
	 *
	 * @param filename
	 *            the B+ tree file name. Input parameter.
	 * @exception GetFileEntryException
	 *                can not ger the file from DB
	 * @exception PinPageException
	 *                failed when pin a page
	 * @exception ConstructPageException
	 *                BT page constructor failed
	 */
	public BTreeFile(String filename) throws GetFileEntryException,
	PinPageException, ConstructPageException {

		headerPageId = get_file_entry(filename);

		headerPage = new BTreeHeaderPage(headerPageId);
		dbname = new String(filename);
		/*
		 * 
		 * - headerPageId is the PageId of this BTreeFile's header page; -
		 * headerPage, headerPageId valid and pinned - dbname contains a copy of
		 * the name of the database
		 */
	}

	/**
	 * if index file exists, open it; else create it.
	 *
	 * @param filename
	 *            file name. Input parameter.
	 * @param keytype
	 *            the type of key. Input parameter.
	 * @param keysize
	 *            the maximum size of a key. Input parameter.
	 * @param delete_fashion
	 *            full delete or naive delete. Input parameter. It is either
	 *            DeleteFashion.NAIVE_DELETE or DeleteFashion.FULL_DELETE.
	 * @exception GetFileEntryException
	 *                can not get file
	 * @exception ConstructPageException
	 *                page constructor failed
	 * @exception IOException
	 *                error from lower layer
	 * @exception AddFileEntryException
	 *                can not add file into DB
	 */
	public BTreeFile(String filename, int keytype, int keysize,
			int delete_fashion) throws GetFileEntryException,
	ConstructPageException, IOException, AddFileEntryException {

		headerPageId = get_file_entry(filename);
		if (headerPageId == null) // file not exist
		{
			headerPage = new BTreeHeaderPage();
			headerPageId = headerPage.getPageId();
			add_file_entry(filename, headerPageId);
			headerPage.set_magic0(MAGIC0);
			headerPage.set_rootId(new PageId(INVALID_PAGE));
			headerPage.set_keyType((short) keytype);
			headerPage.set_maxKeySize(keysize);
			headerPage.set_deleteFashion(delete_fashion);
			headerPage.setType(NodeType.BTHEAD);
		} else {
			headerPage = new BTreeHeaderPage(headerPageId);
		}

		dbname = new String(filename);

	}

	/**
	 * Close the B+ tree file. Unpin header page.
	 *
	 * @exception PageUnpinnedException
	 *                error from the lower layer
	 * @exception InvalidFrameNumberException
	 *                error from the lower layer
	 * @exception HashEntryNotFoundException
	 *                error from the lower layer
	 * @exception ReplacerException
	 *                error from the lower layer
	 */
	public void close() throws PageUnpinnedException,
	InvalidFrameNumberException, HashEntryNotFoundException,
	ReplacerException {
		if (headerPage != null) {
			SystemDefs.JavabaseBM.unpinPage(headerPageId, true);
			headerPage = null;
		}
	}

	/**
	 * Destroy entire B+ tree file.
	 *
	 * @exception IOException
	 *                error from the lower layer
	 * @exception IteratorException
	 *                iterator error
	 * @exception UnpinPageException
	 *                error when unpin a page
	 * @exception FreePageException
	 *                error when free a page
	 * @exception DeleteFileEntryException
	 *                failed when delete a file from DM
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception PinPageException
	 *                failed when pin a page
	 */
	public void destroyFile() throws IOException, IteratorException,
	UnpinPageException, FreePageException, DeleteFileEntryException,
	ConstructPageException, PinPageException {
		if (headerPage != null) {
			PageId pgId = headerPage.get_rootId();
			if (pgId.pid != INVALID_PAGE)
				_destroyFile(pgId);
			unpinPage(headerPageId);
			freePage(headerPageId);
			delete_file_entry(dbname);
			headerPage = null;
		}
	}

	private void _destroyFile(PageId pageno) throws IOException,
	IteratorException, PinPageException, ConstructPageException,
	UnpinPageException, FreePageException {

		BTSortedPage sortedPage;
		Page page = pinPage(pageno);
		sortedPage = new BTSortedPage(page, headerPage.get_keyType());

		if (sortedPage.getType() == NodeType.INDEX) {
			BTIndexPage indexPage = new BTIndexPage(page,
					headerPage.get_keyType());
			RID rid = new RID();
			PageId childId;
			KeyDataEntry entry;
			for (entry = indexPage.getFirst(rid); entry != null; entry = indexPage
					.getNext(rid)) {
				childId = ((IndexData) (entry.data)).getData();
				_destroyFile(childId);
			}
		} else { // BTLeafPage

			unpinPage(pageno);
			freePage(pageno);
		}

	}

	private void updateHeader(PageId newRoot) throws IOException,
	PinPageException, UnpinPageException {

		BTreeHeaderPage header;
		PageId old_data;

		header = new BTreeHeaderPage(pinPage(headerPageId));

		old_data = headerPage.get_rootId();
		header.set_rootId(newRoot);

		// clock in dirty bit to bm so our dtor needn't have to worry about it
		unpinPage(headerPageId, true /* = DIRTY */);

		// ASSERTIONS:
		// - headerPage, headerPageId valid, pinned and marked as dirty

	}

	/**
	 * insert record with the given key and rid
	 *
	 * @param key
	 *            the key of the record. Input parameter.
	 * @param rid
	 *            the rid of the record. Input parameter.
	 * @exception KeyTooLongException
	 *                key size exceeds the max keysize.
	 * @exception KeyNotMatchException
	 *                key is not integer key nor string key
	 * @exception IOException
	 *                error from the lower layer
	 * @exception LeafInsertRecException
	 *                insert error in leaf page
	 * @exception IndexInsertRecException
	 *                insert error in index page
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception UnpinPageException
	 *                error when unpin a page
	 * @exception PinPageException
	 *                error when pin a page
	 * @exception NodeNotMatchException
	 *                node not match index page nor leaf page
	 * @exception ConvertException
	 *                error when convert between revord and byte array
	 * @exception DeleteRecException
	 *                error when delete in index page
	 * @exception IndexSearchException
	 *                error when search
	 * @exception IteratorException
	 *                iterator error
	 * @exception LeafDeleteException
	 *                error when delete in leaf page
	 * @exception InsertException
	 *                error when insert in index page
	 */
	public void insert(KeyClass key, RID rid) throws KeyTooLongException,
	KeyNotMatchException, LeafInsertRecException,
	IndexInsertRecException, ConstructPageException,
	UnpinPageException, PinPageException, NodeNotMatchException,
	ConvertException, DeleteRecException, IndexSearchException,
	IteratorException, LeafDeleteException, InsertException,
	IOException

	{
		KeyDataEntry  newRootEntry;


		if (headerPage.get_rootId().pid == INVALID_PAGE) {
			PageId newRootPageId;
			BTLeafPage newRootPage;

			newRootPage=new BTLeafPage( headerPage.get_keyType());
			newRootPageId=newRootPage.getCurPage();
			newRootPage.setNextPage(new PageId(INVALID_PAGE));
			newRootPage.setPrevPage(new PageId(INVALID_PAGE));
			newRootPage.insertRecord(key, rid); 
			unpinPage(newRootPageId, true); /* = DIRTY */
			updateHeader(newRootPageId);
			return;
		}
		newRootEntry= _insert(key, rid, headerPage.get_rootId());


		if (newRootEntry != null)
		{

			BTIndexPage newRootPage = new BTIndexPage(headerPage.get_keyType());


			newRootPage.insertKey( newRootEntry.key, 
					((IndexData)newRootEntry.data).getData() );

			newRootPage.setPrevPage(headerPage.get_rootId());

			unpinPage(newRootPage.getCurPage(), true);

			updateHeader(newRootPage.getCurPage());

		}

		return;
	}

	/**
	 * @see A recursive method to traverse all the nodes and insert at the right location ,handles splits and copies upto the root level
	 * @param key
	 * @param rid
	 * @param currentPageId
	 * @return
	 * @throws PinPageException
	 * @throws IOException
	 * @throws ConstructPageException
	 * @throws LeafDeleteException
	 * @throws ConstructPageException
	 * @throws DeleteRecException
	 * @throws IndexSearchException
	 * @throws UnpinPageException
	 * @throws LeafInsertRecException
	 * @throws ConvertException
	 * @throws IteratorException
	 * @throws IndexInsertRecException
	 * @throws KeyNotMatchException
	 * @throws NodeNotMatchException
	 * @throws InsertException
	 */


	private KeyDataEntry  _insert(KeyClass key, RID rid,  
			PageId currentPageId) 
					throws  PinPageException,  
					IOException,
					ConstructPageException, 
					LeafDeleteException,  
					ConstructPageException,
					DeleteRecException, 
					IndexSearchException,
					UnpinPageException, 
					LeafInsertRecException,
					ConvertException, 
					IteratorException, 
					IndexInsertRecException,
					KeyNotMatchException, 
					NodeNotMatchException,
					InsertException 

	{



		Page page=pinPage(currentPageId);
		BTSortedPage entryPage=new BTSortedPage(page, headerPage.get_keyType());      
		//Check whether the page is index or leaf page

		if(entryPage.getType() == NodeType.INDEX) { 
			BTIndexPage  currentIndexPage=new BTIndexPage(page, 
					headerPage.get_keyType());
			PageId entryIndexPageId = currentPageId;

			PageId childPageId=currentIndexPage.getPageNoByKey(key);

			unpinPage(entryIndexPageId);
			KeyDataEntry entryToReturn;
			entryToReturn= _insert(key, rid, childPageId);//recursively call until entryToReturn returns null 
			if ( entryToReturn == null)
				return null;

			currentIndexPage= new  BTIndexPage(pinPage(currentPageId),
					headerPage.get_keyType() );

			//split page condition :
			//if  insertKey returns null then there is no more place to insert, split the page
			//else continue 

			if(entryToReturn!=null && currentIndexPage.insertKey(entryToReturn.key,((IndexData)entryToReturn.data).getData())==null)
			{
				//split case
				BTIndexPage childIndexPage;
				PageId      childIndexPageId;

				// we have to allocate a new INDEX page and
				// to redistribute the index entries
				childIndexPage= indexSplit(currentIndexPage);
				childIndexPageId=childIndexPage.getCurPage();  

				KeyDataEntry tempEntry;

				RID firstRid=new RID(),ridToDel=new RID();
				tempEntry= childIndexPage.getFirst(firstRid);
				//condition to check where to place the new entry in .enter it to newly splitted page if key >= entryToReturn.key,else add it to old leaf page 
				if (BT.keyCompare( entryToReturn.key, tempEntry.key) >=0 )
				{

					childIndexPage.insertKey(entryToReturn.key, 
							((IndexData)entryToReturn.data).getData());
				}
				else {
					currentIndexPage.insertKey( entryToReturn.key,((IndexData)entryToReturn.data).getData());
					int i= (int)currentIndexPage.getSlotCnt()-1;
					tempEntry =BT.getEntryFromBytes(currentIndexPage.getpage(), 
							currentIndexPage.getSlotOffset(i),
							currentIndexPage.getSlotLength(i),
							headerPage.get_keyType(),NodeType.INDEX);

					childIndexPage.insertKey( tempEntry.key, 
							((IndexData)tempEntry.data).getData());
					currentIndexPage.deleteSortedRecord
					(new RID(currentIndexPage.getCurPage(), i) );      

				}
				unpinPage(entryIndexPageId, true /* dirty */);

				// return the first entry from the childIndexPage
				entryToReturn= childIndexPage.getFirst(ridToDel);

				//the splittedPage's prev value must point to the old index page  
				childIndexPage.setPrevPage( ((IndexData)entryToReturn.data).getData());

				// As the first index is copied up , we can delete
				childIndexPage.deleteSortedRecord(ridToDel);
				unpinPage(childIndexPageId, true /* dirty */);

				((IndexData)entryToReturn.data).setData( childIndexPageId);

				return entryToReturn;  


			}
			else{
				unpinPage(entryIndexPageId,true);
				return null;
			}				

		}

		else if ( entryPage.getType()==NodeType.LEAF)
		{
			KeyDataEntry entryToReturn;
			BTLeafPage leafPage = 
					new BTLeafPage(page, headerPage.get_keyType() );

			PageId leafPageId = currentPageId;

			//split page condition :
			//if  insertRecord returns null then there is no more place to insert, split the page
			//else continue 

			if (leafPage.insertRecord(key,rid)==null )
			{
				BTLeafPage  childLeafPage=  leafSplit(leafPage);//leafSplit returns the splitted page , also copies up elements from the 'leafPage'
				PageId childLeafPageId=childLeafPage.getCurPage();
				// handle page pointers
				childLeafPage.setPrevPage(leafPage.getCurPage());
				PageId nextId = leafPage.getNextPage();
				if(nextId.pid!=-1)
				{
					Page loadNextPage = new HFPage();
					loadNextPage=pinPage(nextId);
					BTLeafPage nextPage = new BTLeafPage(loadNextPage, headerPage.get_keyType());
					nextPage.setPrevPage(childLeafPage.getCurPage());
					unpinPage(nextPage.getCurPage(),true);

				}
				childLeafPage.setNextPage(nextId);
				leafPage.setNextPage(childLeafPage.getCurPage());
				RID firstRid=new RID();
				KeyDataEntry tempEntry= childLeafPage.getFirst(firstRid);
				if (BT.keyCompare(key, tempEntry.key ) <  0) {
					if ( leafPage.available_space() < 
							childLeafPage.available_space()) {
						childLeafPage.insertRecord( tempEntry.key, 
								((LeafData)tempEntry.data).getData());

						leafPage.deleteSortedRecord
						(new RID(leafPage.getCurPage(),
								(int)leafPage.getSlotCnt()-1) );              
					}
				}	  

				//if the record to add is greater than the last record in the leafPage add it to splitted page
				//else put it in to old leaf page
				if (BT.keyCompare(key,tempEntry.key ) >= 0)
				{                     
					// the new data entry belongs on the new Leaf page
					childLeafPage.insertRecord(key, rid);
				}
				else {
					leafPage.insertRecord(key,rid);
				}

				unpinPage(leafPageId, true /* dirty */);
				// change entryToReturn to return the first element of the childLeafPage 
				tempEntry=childLeafPage.getFirst(firstRid);
				entryToReturn=new KeyDataEntry(tempEntry.key, childLeafPageId );
				unpinPage(childLeafPageId, true /* dirty */);
				return entryToReturn;
			}
			else{
				// no split has occurred
				unpinPage(leafPageId, true /* DIRTY */);
				return null;
			}

		}
		else {    
			throw new InsertException(null,"");
		}

	}
	/**
	 * @see calls to this method gives the page where the split happened
	 * @param currentLeafPage
	 * @return
	 */




	private BTLeafPage leafSplit(BTLeafPage currentLeafPage) {
		// TODO Auto-generated method stub

		BTLeafPage splittedPage = null;
		try 
		{
			//splitPage
			splittedPage= new BTLeafPage(headerPage.get_keyType()); // make a new page and pin it
			splittedPage.init(splittedPage.getCurPage(),splittedPage);
			splittedPage.setType(NodeType.LEAF);

			// take records from the parent page until it's halve full
			RID iteratorRecord = currentLeafPage.firstRecord();
			KeyDataEntry currentEntry = currentLeafPage.getFirst(iteratorRecord);

			// iteratorRecord is used to get a handle on the records of the currentLeafPage
			//We iterate through the elements till we come to the middle element of currentLeafPage
			for (int i = 0; i < currentLeafPage.numberOfRecords()/2  ; i++) 
			{	 currentEntry = currentLeafPage.getNext(iteratorRecord);	}

			// Enter all the records in the currentLeafPage to new splitted page starting from the middle  of currentLeafPage;
			for (int i = currentLeafPage.numberOfRecords()/2; i < currentLeafPage.numberOfRecords(); i++) 
			{
				splittedPage.insertRecord(currentEntry.key , ((LeafData) currentEntry.data).getData());
				currentEntry = currentLeafPage.getNext(iteratorRecord);
			}

			//Delete all the duplicate records which already exist in the splitted page
			//As the greater half of the records exist in splitted page , delete all the records from the middle to the end of the parent leafPage
			int boundry = currentLeafPage.numberOfRecords();
			for (int i =boundry/2; i < boundry; i++) 
			{
				//Travers through the currentLeafPage and get the lastRecord in it		
				currentEntry = currentLeafPage.getFirst(iteratorRecord);
				KeyDataEntry nextEntry = currentLeafPage.getNext(iteratorRecord);
				while(nextEntry!= null)
				{
					currentEntry = currentLeafPage.getCurrent(iteratorRecord);
					nextEntry = currentLeafPage.getNext(iteratorRecord);
				}

				currentLeafPage.delEntry(currentEntry);
			}

		} catch (IOException | ConstructPageException | IteratorException | LeafInsertRecException | LeafDeleteException  e) {
			e.printStackTrace();
		}
		return splittedPage;

	}

	/**
	 * @see calls to this method gives the page where the split happened
	 * @param currentIndexPage
	 * @return BTIndexPage ; page where the split happens
	 */
	private BTIndexPage indexSplit(BTIndexPage currentIndexPage) {
		// TODO Auto-generated method stub
		BTIndexPage splittedIndexPage = null;
		try 
		{
			// split page
			splittedIndexPage= new BTIndexPage(headerPage.get_keyType()); // make a new page and pin it
			splittedIndexPage.init(splittedIndexPage.getCurPage(),splittedIndexPage);
			splittedIndexPage.setType(NodeType.INDEX);

			// iteratorRecord is used to get a handle on the records of the currentIndexPage
			//We iterate through the elements till we come to the middle element of currentIndexPage
			RID iteratorRecord = currentIndexPage.firstRecord();
			KeyDataEntry currentEntry = currentIndexPage.getFirst(iteratorRecord);
			for (int i = 0; i < currentIndexPage.numberOfRecords()/2 ; i++) 
			{	 currentEntry = currentIndexPage.getNext(iteratorRecord);	}

			// Enter all the records in the currentIndexPage to new splitted page starting from the middle  of current Indexpage;
			for (int i = currentIndexPage.numberOfRecords()/2; i < currentIndexPage.numberOfRecords(); i++) 
			{
				splittedIndexPage.insertKey(currentEntry.key , ((IndexData) currentEntry.data).getData());
				currentEntry = currentIndexPage.getNext(iteratorRecord);
			}

			//Delete all the duplicate records which already exist in the splitted page
			//As the greater half of the records exist in splitted page , delete all the records from the middle to the end of the parent indexPage
			int deleteRecordsFrom=currentIndexPage.numberOfRecords()/2;
			for (int i =deleteRecordsFrom; i < currentIndexPage.numberOfRecords()/2; i++) 
			{

				//Travers through the currentINdexPage and get the lastRecord in it

				iteratorRecord = currentIndexPage.firstRecord();

				RID nextRecord = currentIndexPage.nextRecord(iteratorRecord);
				while(nextRecord!= null)
				{
					iteratorRecord = currentIndexPage.nextRecord(iteratorRecord);
					nextRecord = currentIndexPage.nextRecord(nextRecord);
				}				
				currentIndexPage.deleteSortedRecord(iteratorRecord);
			}
		} catch (IOException | ConstructPageException | DeleteRecException | IteratorException | IndexInsertRecException  e) {
			e.printStackTrace();
		}
		return splittedIndexPage;
	}

	/**
	 * delete leaf entry given its <key, rid> pair. `rid' is IN the data entry;
	 * it is not the id of the data entry)
	 *
	 * @param key
	 *            the key in pair <key, rid>. Input Parameter.
	 * @param rid
	 *            the rid in pair <key, rid>. Input Parameter.
	 * @return true if deleted. false if no such record.
	 * @exception DeleteFashionException
	 *                neither full delete nor naive delete
	 * @exception LeafRedistributeException
	 *                redistribution error in leaf pages
	 * @exception RedistributeException
	 *                redistribution error in index pages
	 * @exception InsertRecException
	 *                error when insert in index page
	 * @exception KeyNotMatchException
	 *                key is neither integer key nor string key
	 * @exception UnpinPageException
	 *                error when unpin a page
	 * @exception IndexInsertRecException
	 *                error when insert in index page
	 * @exception FreePageException
	 *                error in BT page constructor
	 * @exception RecordNotFoundException
	 *                error delete a record in a BT page
	 * @exception PinPageException
	 *                error when pin a page
	 * @exception IndexFullDeleteException
	 *                fill delete error
	 * @exception LeafDeleteException
	 *                delete error in leaf page
	 * @exception IteratorException
	 *                iterator error
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception DeleteRecException
	 *                error when delete in index page
	 * @exception IndexSearchException
	 *                error in search in index pages
	 * @exception IOException
	 *                error from the lower layer
	 *
	 */
	public boolean Delete(KeyClass key, RID rid) throws DeleteFashionException,
	LeafRedistributeException, RedistributeException,
	InsertRecException, KeyNotMatchException, UnpinPageException,
	IndexInsertRecException, FreePageException,
	RecordNotFoundException, PinPageException,
	IndexFullDeleteException, LeafDeleteException, IteratorException,
	ConstructPageException, DeleteRecException, IndexSearchException,
	IOException {
		if (headerPage.get_deleteFashion() == DeleteFashion.NAIVE_DELETE)
			return NaiveDelete(key, rid);
		else
			throw new DeleteFashionException(null, "");
	}

	/*
	 * findRunStart. Status BTreeFile::findRunStart (const void lo_key, RID
	 * *pstartrid)
	 * 
	 * find left-most occurrence of `lo_key', going all the way left if lo_key
	 * is null.
	 * 
	 * Starting record returned in *pstartrid, on page *pppage, which is pinned.
	 * 
	 * Since we allow duplicates, this must "go left" as described in the text
	 * (for the search algorithm).
	 * 
	 * @param lo_key find left-most occurrence of `lo_key', going all the way
	 * left if lo_key is null.
	 * 
	 * @param startrid it will reurn the first rid =< lo_key
	 * 
	 * @return return a BTLeafPage instance which is pinned. null if no key was
	 * found.
	 */

	BTLeafPage findRunStart(KeyClass lo_key, RID startrid) throws IOException,
	IteratorException, KeyNotMatchException, ConstructPageException,
	PinPageException, UnpinPageException {
		BTLeafPage pageLeaf;
		BTIndexPage pageIndex;
		Page page;
		BTSortedPage sortPage;
		PageId pageno;
		
		PageId prevpageno;
		PageId nextpageno;
		
		KeyDataEntry curEntry;

		pageno = headerPage.get_rootId();

		if (pageno.pid == INVALID_PAGE) { // no pages in the BTREE
			pageLeaf = null; // should be handled by
			// startrid =INVALID_PAGEID ; // the caller
			return pageLeaf;
		}

		page = pinPage(pageno);
		sortPage = new BTSortedPage(page, headerPage.get_keyType());

		if (trace != null) {
			trace.writeBytes("VISIT node " + pageno + lineSep);
			trace.flush();
		}

		// ASSERTION
		// - pageno and sortPage is the root of the btree
		// - pageno and sortPage valid and pinned

		while (sortPage.getType() == NodeType.INDEX) {
			pageIndex = new BTIndexPage(page, headerPage.get_keyType());
			prevpageno = pageIndex.getPrevPage();
			curEntry = pageIndex.getFirst(startrid);
			while (curEntry != null && lo_key != null
					&& BT.keyCompare(curEntry.key, lo_key) < 0) {

				prevpageno = ((IndexData) curEntry.data).getData();
				curEntry = pageIndex.getNext(startrid);
			}

			unpinPage(pageno);

			pageno = prevpageno;
			page = pinPage(pageno);
			sortPage = new BTSortedPage(page, headerPage.get_keyType());

			if (trace != null) {
				trace.writeBytes("VISIT node " + pageno + lineSep);
				trace.flush();
			}

		}

		pageLeaf = new BTLeafPage(page, headerPage.get_keyType());

		curEntry = pageLeaf.getFirst(startrid);
		while (curEntry == null) {
			// skip empty leaf pages off to left
			nextpageno = pageLeaf.getNextPage();
			unpinPage(pageno);
			if (nextpageno.pid == INVALID_PAGE) {
				// oops, no more records, so set this scan to indicate this.
				return null;
			}

			pageno = nextpageno;
			pageLeaf = new BTLeafPage(pinPage(pageno), headerPage.get_keyType());
			curEntry = pageLeaf.getFirst(startrid);
		}

		// ASSERTIONS:
		// - curkey, curRid: contain the first record on the
		// current leaf page (curkey its key, cur
		// - pageLeaf, pageno valid and pinned

		if (lo_key == null) {
			return pageLeaf;
			// note that pageno/pageLeaf is still pinned;
			// scan will unpin it when done
		}

		while (BT.keyCompare(curEntry.key, lo_key) < 0) {
			curEntry = pageLeaf.getNext(startrid);
			while (curEntry == null) { // have to go right
				nextpageno = pageLeaf.getNextPage();
				unpinPage(pageno);

				if (nextpageno.pid == INVALID_PAGE) {
					return null;
				}

				pageno = nextpageno;
				pageLeaf = new BTLeafPage(pinPage(pageno),
						headerPage.get_keyType());

				curEntry = pageLeaf.getFirst(startrid);
			}
		}

		return pageLeaf;
	}

	/*
	 * Status BTreeFile::NaiveDelete (const void *key, const RID rid)
	 * 
	 * Remove specified data entry (<key, rid>) from an index.
	 * 
	 * We don't do merging or redistribution, but do allow duplicates.
	 * 
	 * Page containing first occurrence of key `key' is found for us by
	 * findRunStart. We then iterate for (just a few) pages, if necesary, to
	 * find the one containing <key,rid>, which we then delete via
	 * BTLeafPage::delUserRid.
	 */

	private boolean NaiveDelete(KeyClass key, RID rid)
			throws LeafDeleteException, KeyNotMatchException, PinPageException,
			ConstructPageException, IOException, UnpinPageException,
			PinPageException, IndexSearchException, IteratorException {


		boolean deleted = false;
		try{
			if (headerPage != null) 
			{
				RID iteratorrid=new RID();
				BTLeafPage requiredLeafPage=null;
				//get the leaf page 
				requiredLeafPage=findRunStart(key,iteratorrid ); 
				// first record in the leafPage, start Search in the record from here 
				RID currentRecord = requiredLeafPage.firstRecord();
				KeyDataEntry currentEntry = ((BTLeafPage)requiredLeafPage).getFirst(currentRecord);

				for (int i = 0; i < requiredLeafPage.getSlotCnt(); i++) {

					// check if the currentEntry is the required key 
					if( currentEntry != null && BT.keyCompare( key , currentEntry.key) == 0)	
					{	
						deleted =	requiredLeafPage.delEntry(currentEntry);
						break;
					}
					//if not traverse through next nodes
					else if(currentEntry != null)
					{

						currentEntry = requiredLeafPage.getNext(currentRecord);
					}
					else
					{
						deleted = false ;
						break;
					}
				}

				unpinPage(requiredLeafPage.getCurPage(),true);


			}
		}
		catch (IOException | KeyNotMatchException e) {
			e.printStackTrace();
		}
		return deleted;

	}

	/**
	 * create a scan with given keys Cases: (1) lo_key = null, hi_key = null
	 * scan the whole index (2) lo_key = null, hi_key!= null range scan from min
	 * to the hi_key (3) lo_key!= null, hi_key = null range scan from the lo_key
	 * to max (4) lo_key!= null, hi_key!= null, lo_key = hi_key exact match (
	 * might not unique) (5) lo_key!= null, hi_key!= null, lo_key < hi_key range
	 * scan from lo_key to hi_key
	 *
	 * @param lo_key
	 *            the key where we begin scanning. Input parameter.
	 * @param hi_key
	 *            the key where we stop scanning. Input parameter.
	 * @exception IOException
	 *                error from the lower layer
	 * @exception KeyNotMatchException
	 *                key is not integer key nor string key
	 * @exception IteratorException
	 *                iterator error
	 * @exception ConstructPageException
	 *                error in BT page constructor
	 * @exception PinPageException
	 *                error when pin a page
	 * @exception UnpinPageException
	 *                error when unpin a page
	 */
	public BTFileScan new_scan(KeyClass lo_key, KeyClass hi_key)
			throws IOException, KeyNotMatchException, IteratorException,
			ConstructPageException, PinPageException, UnpinPageException

	{
		BTFileScan scan = new BTFileScan();
		if (headerPage.get_rootId().pid == INVALID_PAGE) {
			scan.leafPage = null;
			return scan;
		}

		scan.treeFilename = dbname;
		scan.endkey = hi_key;
		scan.didfirst = false;
		scan.deletedcurrent = false;
		scan.curRid = new RID();
		scan.keyType = headerPage.get_keyType();
		scan.maxKeysize = headerPage.get_maxKeySize();
		scan.bfile = this;

		// this sets up scan at the starting position, ready for iteration
		scan.leafPage = findRunStart(lo_key, scan.curRid);
		return scan;
	}

	void trace_children(PageId id) throws IOException, IteratorException,
	ConstructPageException, PinPageException, UnpinPageException {

		if (trace != null) {

			BTSortedPage sortedPage;
			RID metaRid = new RID();
			PageId childPageId;
			KeyClass key;
			KeyDataEntry entry;
			sortedPage = new BTSortedPage(pinPage(id), headerPage.get_keyType());

			// Now print all the child nodes of the page.
			if (sortedPage.getType() == NodeType.INDEX) {
				BTIndexPage indexPage = new BTIndexPage(sortedPage,
						headerPage.get_keyType());
				trace.writeBytes("INDEX CHILDREN " + id + " nodes" + lineSep);
				trace.writeBytes(" " + indexPage.getPrevPage());
				for (entry = indexPage.getFirst(metaRid); entry != null; entry = indexPage
						.getNext(metaRid)) {
					trace.writeBytes("   " + ((IndexData) entry.data).getData());
				}
			} else if (sortedPage.getType() == NodeType.LEAF) {
				BTLeafPage leafPage = new BTLeafPage(sortedPage,
						headerPage.get_keyType());
				trace.writeBytes("LEAF CHILDREN " + id + " nodes" + lineSep);
				for (entry = leafPage.getFirst(metaRid); entry != null; entry = leafPage
						.getNext(metaRid)) {
					trace.writeBytes("   " + entry.key + " " + entry.data);
				}
			}
			unpinPage(id);
			trace.writeBytes(lineSep);
			trace.flush();
		}

	}

}
