package cscie97.smartcity.ledger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class encapsulates the Merkle-tree hashing of collections of objects, where
 * the pairwise hash is a base64 encoding of the concatenation of the string representation
 * of each object (obtained through the toString method).
 */
public class Hash {

	private static final String hashOnePair (String s1, String s2) {
    	MessageDigest digest = null; 

		try {
			digest = MessageDigest.getInstance ("SHA-256");
    	} catch (NoSuchAlgorithmException nsax) {
    		throw new RuntimeException ("SHA-256 hash is not available; check your JVM configuration.");
    	}
 
		digest.update (s1.getBytes ());
		digest.update (s2.getBytes ());
		return (Base64.getEncoder ().encodeToString (digest.digest ()));
	}
	
	private static final List<String> hashPairs (List<String> strings) {
		if (strings.size () <= 1) {
			return (strings); // nothing to do
		}
		
		List<String> ret = new ArrayList <> ();
		
		int numPairs = strings.size () / 2;
		for (int iP = 0; iP < numPairs; iP ++) {
			ret.add (hashOnePair (strings.get (2 * iP), strings.get (2 * iP + 1)));
		}
		
		// if we had an odd number of strings to hash, add the last one to the list as is
		if (numPairs * 2 < strings.size ()) {
			ret.add (strings.get (strings.size () - 1));
		}
		
		return (ret);
	}
	
	/**
	 * Given an arbitrary number of objects, convert them to strings and
	 * compute Merkle-tree hash of the entire list using base64 encoding
	 * @param objs The collection of objects to be hashed
	 * @return The base64 encoded hash
	 */
    public static final String hash (Object... objs) {
    	if (objs == null || objs.length <= 0) {
    		return ("");
    	}
    	
    	// collect string representations of the objects
		List<String> leftToHash = Arrays.asList (objs)
				.stream ()
				.map (Object::toString)
				.collect (Collectors.toList ());
		
		// Edge case: there is only one string in the input
		// Just encode it and return
		if (leftToHash.size () == 1) {
			return (hashOnePair (leftToHash.get (0), ""));
		}
		
		// hash all elements pairwise, until there's only one left
		while (leftToHash.size () > 1) {
			leftToHash = hashPairs (leftToHash);
		}

		// this single element is the final hash
		return (leftToHash.get (0));
    }

}
