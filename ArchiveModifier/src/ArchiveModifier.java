import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A wrapper around the Java zip utilities to add, overwrite, or remove files
 * from archives.
 * 
 * @author Ben Holland
 */
public class ArchiveModifier {

	private HashMap<String,ZipEntry> archiveEntries = new HashMap<String,ZipEntry>();
	private HashMap<String,File> archiveEntriesToAdd = new HashMap<String,File>();
	private File archiveFile;
	
	public ArchiveModifier(File archiveFile) throws ZipException, IOException {
		this.archiveFile = archiveFile;
		ZipFile archive = new ZipFile(archiveFile);
		// get references to all the archive file entries
		Enumeration<? extends ZipEntry> enumerator = archive.entries();
		while(enumerator.hasMoreElements()){
			ZipEntry current = (ZipEntry) enumerator.nextElement();
			// need to create a new entry to reset properties that will need to be recomputed automatically
			ZipEntry resetEntry = new ZipEntry(current.getName());
			// copy over entry properties
			resetEntry.setTime(current.getTime());
			resetEntry.setComment(current.getComment());
			resetEntry.setExtra(current.getExtra());
			resetEntry.setSize(current.getSize());
			resetEntry.setMethod(current.getMethod());
			archiveEntries.put(current.getName(), resetEntry); // resets zip entry state
			
		}
		archive.close();
	}

	/**
	 * Adds (or optionally overwrites) a archive entry
	 * 
	 * @param entry The entry path (example a/b/c/test.txt)
	 * @param file The contents of the file to add
	 * @param overwrite True if an existing entry should be overwritten
	 * @throws IOException
	 *             Thrown if overwrite is false and the archive already contains
	 *             the specified entry
	 */
	public void add(String entry, File file, boolean overwrite) throws IOException {
		if(archiveEntries.containsKey(entry) && !overwrite){
			throw new IOException("Archive already contains entry: " + entry);
		} else {
			 // remove an entry if one already exists
			archiveEntries.remove(entry);
			archiveEntriesToAdd.remove(entry);
			// add a new entry
			ZipEntry newEntry = new ZipEntry(entry);
			archiveEntries.put(entry, newEntry);
			archiveEntriesToAdd.put(entry, file);
		}
	}
	
	/**
	 * Removes the specified entry if one exits (example: a/b/c/test.txt)
	 * 
	 * @param entry
	 */
	public void remove(String entry){
		archiveEntries.remove(entry);
		archiveEntriesToAdd.remove(entry);
	}
	
	/**
	 * Removes any entries with a matching file name (example: test.txt)
	 * 
	 * @param filename The filename to match
	 */
	public void removeFilesWithName(String filename){
		// clear the entries that may have already existed in the archive
		LinkedList<String> entriesToRemove = new LinkedList<String>();
		for(Entry<String,ZipEntry> zipEntry : archiveEntries.entrySet()){
			if(zipEntry.getKey().endsWith(filename)){
				entriesToRemove.add(zipEntry.getKey());
			}
		}
		for(String entryToRemove : entriesToRemove){
			archiveEntries.remove(entryToRemove);
		}
		entriesToRemove.clear();
		
		// clear the entries that may have queued to be added
		for(Entry<String,File> zipEntry : archiveEntriesToAdd.entrySet()){
			if(zipEntry.getKey().endsWith(filename)){
				entriesToRemove.add(zipEntry.getKey());
			}
		}
		for(String entryToRemove : entriesToRemove){
			archiveEntriesToAdd.remove(entryToRemove);
		}
	}
	
	/**
	 * Writes the modified output archive to a file
	 * 
	 * @param outputArchive
	 * @throws IOException  
	 */
	public void save(File outputArchiveFile) throws IOException {
		ZipInputStream zin = null;
	    ZipOutputStream zout = null;
	    try {
	    	byte[] buf = new byte[1024];
	    	zin = new ZipInputStream(new FileInputStream(archiveFile));
		    zout = new ZipOutputStream(new FileOutputStream(outputArchiveFile));
	    	ZipEntry entry = zin.getNextEntry();
		    while (entry != null) {
		    	// use the zip entry settings stored in the entries set
		        zout.putNextEntry(archiveEntries.get(entry.getName()));
		        // write the file to the zip depending on where it is located
		        if(archiveEntriesToAdd.containsKey(entry.getName())){
		        	// transfer the bytes from the saved file to the output archive
		        	InputStream fin = null;
		        	try {
		        		fin = new FileInputStream(archiveEntriesToAdd.get(entry.getName()));
		        		int len;
		                while ((len = fin.read(buf)) > 0) {
		                    zout.write(buf, 0, len);
		                }
		                // complete the entry
		                zout.closeEntry();
		        	} finally {
		        		fin.close();
		        	}
		        } else {
		            // transfer the bytes from the old archive to the output archive
		            int len;
		            while ((len = zin.read(buf)) > 0) {
		                zout.write(buf, 0, len);
		            }
		            // complete the entry
	                zout.closeEntry();
		        }
		        // get the next zip entry to examine
		        entry = zin.getNextEntry();
		    }
	    } finally {
	    	// close the streams        
		    zin.close();
		    zout.close();
	    } 
	}
	
	/**
	 * Prints the contents of the archive file if it were written to disk
	 */
	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		
		ArrayList<String> allEntries = new ArrayList<String>(archiveEntries.keySet().size());
		allEntries.addAll(archiveEntries.keySet());
		Collections.sort(allEntries);
		
		for(String entry : allEntries) {
			result.append(entry);
			result.append(" [");
			if(archiveEntriesToAdd.containsKey(entry)){
				result.append(archiveEntriesToAdd.get(entry).getAbsolutePath());
			} else {
				result.append(archiveFile.getAbsolutePath());
			}
			result.append("]\n");
		}
		
		return result.toString();
	}
	
}
