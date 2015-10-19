# ArchiveModifier
A zip archive modification library for adding, replacing, or deleting a file in an archive

## Background
I wanted to programmatically modify a zip file by inserting, replacing, or deleting files in the archive.  After searching for solutions online I came across an article by Allen Holub (article [part 1](http://www.javaworld.com/article/2076136/core-java/modify-archives--part-1.html) and [part 2](http://www.javaworld.com/article/2076222/core-java/modifying-archives--part-2--the-archive-class.html)) who proposes a wrapper around the Java zip utilities to efficiently do just what I wanted.  After looking through the [code released in the article](http://www.holub.com/publications/articles/javaworld/Holub_10_00.zip) I found it had some missing dependencies and was somewhat cringe-worthy to read (no offense to the original author, everyone has a different style).  While I originally started work on a fork of the code, I found his license was too restrictive to permit publishing a fork, so I decided to create my own implementation from scratch.

## Example Usage
	// opens and caches properties concerning the test.zip archive file
	File inputArchive = new File("/Users/benjholla/Desktop/test.zip");
	ArchiveModifier archive = new ArchiveModifier(inputArchive);
	
	// removes the blah.txt file in the directory named some_directory
	archive.remove("some_directory/test.txt");
	
	// replaces the zipped contents of test.txt file in directory c with the
	// contents of the stored file test2.txt
	archive.add("a/b/c/test.txt", new File("/Users/benjholla/Desktop/test2.txt"), true);
	
	// write the output archive as test-modified.zip
	File outputArchive = new File("/Users/benjholla/Desktop/test-modified.zip");
	archive.save(outputArchive);