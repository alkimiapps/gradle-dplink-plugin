/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alkimiapps.javatools;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * NOTE: this is a subset of org.apache.commons.io.FileUtils - directly copied from that file.
 * <p>
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>listing files and directories by filter and extension
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p>
 * Note that a specific charset should be specified whenever possible.
 * Relying on the platform default means that the code is Locale-dependent.
 * Only use the default if the files are known to always use the platform default.
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 */
public class FileUtils {
	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final long ONE_KB = 1024;
	
	/**
	 * The number of bytes in a megabyte.
	 */
	public static final long ONE_MB = ONE_KB * ONE_KB;
	
	/**
	 * Copies a whole directory to a new location preserving the file dates.
	 * <p>
	 * This method copies the specified directory and all its child
	 * directories and files to the specified destination.
	 * The destination is the new location and name of the directory.
	 * <p>
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however
	 * it is not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 * @param srcDir an existing directory to copy, must not be {@code null}
	 * @param destDir the new directory, must not be {@code null}
	 * @throws NullPointerException if source or destination is {@code null}
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since 1.1
	 */
	public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
		copyDirectory(srcDir, destDir, true);
	}
	
	/**
	 * Copies a whole directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory
	 * to within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * {@code true} tries to preserve the files' last modified
	 * date/times using {@link File#setLastModified(long)}, however it is
	 * not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 * @param srcDir an existing directory to copy, must not be {@code null}
	 * @param destDir the new directory, must not be {@code null}
	 * @param preserveFileDate true if the file date of the copy
	 * should be the same as the original
	 * @throws NullPointerException if source or destination is {@code null}
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since 1.1
	 */
	public static void copyDirectory(final File srcDir, final File destDir,
									 final boolean preserveFileDate) throws IOException {
		copyDirectory(srcDir, destDir, null, preserveFileDate);
	}
	
	/**
	 * Copies a filtered directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory
	 * to within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * {@code true} tries to preserve the files' last modified
	 * date/times using {@link File#setLastModified(long)}, however it is
	 * not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 * </p>
	 * <h3>Example: Copy directories only</h3>
	 * <pre>
	 *  // only copy the directory structure
	 *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
	 *  </pre>
	 *
	 * <h3>Example: Copy directories and txt files</h3>
	 * <pre>
	 *  // Create a filter for ".txt" files
	 *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
	 *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 *
	 *  // Create a filter for either directories or ".txt" files
	 *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 *
	 *  // Copy using the filter
	 *  FileUtils.copyDirectory(srcDir, destDir, filter, false);
	 *  </pre>
	 * @param srcDir an existing directory to copy, must not be {@code null}
	 * @param destDir the new directory, must not be {@code null}
	 * @param filter the filter to apply, null means copy all directories and files
	 * @param preserveFileDate true if the file date of the copy
	 * should be the same as the original
	 * @throws NullPointerException if source or destination is {@code null}
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since 1.4
	 */
	public static void copyDirectory(final File srcDir, final File destDir,
									 final FileFilter filter, final boolean preserveFileDate) throws IOException {
		checkFileRequirements(srcDir, destDir);
		if (!srcDir.isDirectory()) {
			throw new IOException("Source '" + srcDir + "' exists but is not a directory");
		}
		if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
			throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
		}
		
		// Cater for destination being directory within the source directory (see IO-141)
		List<String> exclusionList = null;
		if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
			final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
			if (srcFiles != null && srcFiles.length > 0) {
				exclusionList = new ArrayList<>(srcFiles.length);
				for (final File srcFile : srcFiles) {
					final File copiedFile = new File(destDir, srcFile.getName());
					exclusionList.add(copiedFile.getCanonicalPath());
				}
			}
		}
		doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
	}
	
	/**
	 * Deletes a file. If file is a directory, delete it and all sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted.
	 * (java.io.File methods returns a boolean)</li>
	 * </ul>
	 * @param file file or directory to delete, must not be {@code null}
	 * @throws NullPointerException if the directory is {@code null}
	 * @throws FileNotFoundException if the file was not found
	 * @throws IOException in case deletion is unsuccessful
	 */
	public static void forceDelete(final File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			final boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: " + file);
				}
				final String message =
						"Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}
	
	/**
	 * Deletes a directory recursively.
	 * @param directory directory to delete
	 * @throws IOException in case deletion is unsuccessful
	 * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
	 */
	public static void deleteDirectory(final File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}
		
		if (!isSymlink(directory)) {
			cleanDirectory(directory);
		}
		
		if (!directory.delete()) {
			final String message =
					"Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}
	
	/**
	 * Cleans a directory without deleting it.
	 * @param directory directory to clean
	 * @throws IOException in case cleaning is unsuccessful
	 * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
	 */
	public static void cleanDirectory(final File directory) throws IOException {
		final File[] files = verifiedListFiles(directory);
		
		IOException exception = null;
		for (final File file : files) {
			try {
				forceDelete(file);
			} catch(final IOException ioe) {
				exception = ioe;
			}
		}
		
		if (null != exception) {
			throw exception;
		}
	}
	
	/**
	 * Determines whether the specified file is a Symbolic Link rather than an actual file.
	 * <p>
	 * Will not return true if there is a Symbolic Link anywhere in the path,
	 * only if the specific file is.
	 * <p>
	 * When using jdk1.7, this method delegates to {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
	 * <p>
	 * For code that runs on Java 1.7 or later, use the following method instead:
	 * <br>
	 * {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
	 * @param file the file to check
	 * @return true if the file is a Symbolic Link
	 * @throws IOException if an IO error occurs while checking the file
	 * @since 2.0
	 */
	public static boolean isSymlink(final File file) throws IOException {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		return Files.isSymbolicLink(file.toPath());
	}
	
	/**
	 * The file copy buffer size (30 MB)
	 */
	private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;
	
	/**
	 * Lists files in a directory, asserting that the supplied directory satisfies exists and is a directory
	 * @param directory The directory to list
	 * @return The files in the directory, never null.
	 * @throws IOException if an I/O error occurs
	 */
	private static File[] verifiedListFiles(final File directory) throws IOException {
		if (!directory.exists()) {
			final String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}
		
		if (!directory.isDirectory()) {
			final String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}
		
		final File[] files = directory.listFiles();
		if (files == null) {  // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}
		return files;
	}
	
	/**
	 * checks requirements for file copy
	 * @param src the source file
	 * @param dest the destination
	 * @throws FileNotFoundException if the destination does not exist
	 */
	private static void checkFileRequirements(final File src, final File dest) throws FileNotFoundException {
		if (src == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (dest == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!src.exists()) {
			throw new FileNotFoundException("Source '" + src + "' does not exist");
		}
	}
	
	/**
	 * Internal copy directory method.
	 * @param srcDir the validated source directory, must not be {@code null}
	 * @param destDir the validated destination directory, must not be {@code null}
	 * @param filter the filter to apply, null means copy all directories and files
	 * @param preserveFileDate whether to preserve the file date
	 * @param exclusionList List of files and directories to exclude from the copy, may be null
	 * @throws IOException if an error occurs
	 * @since 1.1
	 */
	private static void doCopyDirectory(final File srcDir, final File destDir, final FileFilter filter,
										final boolean preserveFileDate, final List<String> exclusionList)
			throws IOException {
		// recurse
		final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
		if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
			throw new IOException("Failed to list contents of " + srcDir);
		}
		if (destDir.exists()) {
			if (!destDir.isDirectory()) {
				throw new IOException("Destination '" + destDir + "' exists but is not a directory");
			}
		} else {
			if (!destDir.mkdirs() && !destDir.isDirectory()) {
				throw new IOException("Destination '" + destDir + "' directory cannot be created");
			}
		}
		if (!destDir.canWrite()) {
			throw new IOException("Destination '" + destDir + "' cannot be written to");
		}
		for (final File srcFile : srcFiles) {
			final File dstFile = new File(destDir, srcFile.getName());
			if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
				if (srcFile.isDirectory()) {
					doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
				} else {
					doCopyFile(srcFile, dstFile, preserveFileDate);
				}
			}
		}
		
		// Do this last, as the above has probably affected directory metadata
		if (preserveFileDate) {
			destDir.setLastModified(srcDir.lastModified());
		}
	}
	
	/**
	 * Internal copy file method.
	 * This caches the original file length, and throws an IOException
	 * if the output file length is different from the current input file length.
	 * So it may fail if the file changes size.
	 * It may also fail with "IllegalArgumentException: Negative size" if the input file is truncated part way
	 * through copying the data and the new file size is less than the current position.
	 * @param srcFile the validated source file, must not be {@code null}
	 * @param destFile the validated destination file, must not be {@code null}
	 * @param preserveFileDate whether to preserve the file date
	 * @throws IOException if an error occurs
	 * @throws IOException if the output file length is not the same as the input file length after the
	 * copy completes
	 * @throws IllegalArgumentException "Negative size" if the file is truncated so that the size is less than the
	 * position
	 */
	private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
			throws IOException {
		if (destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}
		
		try (FileInputStream fis = new FileInputStream(srcFile);
			 FileChannel input = fis.getChannel();
			 FileOutputStream fos = new FileOutputStream(destFile);
			 FileChannel output = fos.getChannel()) {
			final long size = input.size(); // TODO See IO-386
			long pos = 0;
			long count = 0;
			while (pos < size) {
				final long remain = size - pos;
				count = remain > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : remain;
				final long bytesCopied = output.transferFrom(input, pos, count);
				if (bytesCopied == 0) { // IO-385 - can happen if file is truncated after caching the size
					break; // ensure we don't loop forever
				}
				pos += bytesCopied;
			}
		}
		
		final long srcLen = srcFile.length(); // TODO See IO-386
		final long dstLen = destFile.length(); // TODO See IO-386
		if (srcLen != dstLen) {
			throw new IOException("Failed to copy full contents from '" +
					srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
		}
		if (preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}
	
}
