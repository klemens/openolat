package de.htwk.autolat.tools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.internet.MimeUtility;

import org.olat.core.util.vfs.*;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * Virtual VFSLeaf that is based on an InputStream and
 * can only be read, but not modified in any way.
 */
public class StreamVFSLeaf implements VFSLeaf {
	private String name;
	private InputStream stream;
	private long length;

	public StreamVFSLeaf(String name, InputStream stream, long length) {
		this.name = name;
		this.stream = stream;
		this.length = length;
	}

	public StreamVFSLeaf(String name, String base64) {
		this.name = name;
		try {
			byte[] bytes = base64.getBytes("UTF-8");
			this.length = bytes.length;
			this.stream = MimeUtility.decode(new ByteArrayInputStream(bytes), "base64");
		} catch(Exception e) {
			throw new RuntimeException("Error while parsing base64 coded image", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InputStream getInputStream() {
		return stream;
	}

	@Override
	public long getSize() {
		return length;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		return false;
	}

	@Override
	public long getLastModified() {
		return VFSConstants.UNDEFINED;
	}

	@Override
	public OutputStream getOutputStream(boolean append) {
		return null;
	}

	@Override
	public VFSStatus canRename() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canDelete() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canCopy() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}

	@Override
	public VFSItem resolve(String path) {
		return null;
	}

	@Override
	public VFSStatus rename(String newname) {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus delete() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus deleteSilently() {
		return VFSConstants.NO;
	}

	@Override
	public VFSContainer getParentContainer() {
		return null;
	}
	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		//
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return null;
	}
	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		//
	}
}
