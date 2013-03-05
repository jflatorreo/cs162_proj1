
	private int handleCreate(int a0) {
		int fileDescriptor = -1;
		boolean linkedFlag = true;
		String filename = readVirtualMemoryString(a0, 256);
		OpenFile openfile = ThreadedKernel.fileSystem.open(filename, true); //if there is no such OpenFile with the filename, create one (true)
		
		if (openfile == null) //There is no such OpenFile with filename and also cannot create OpenFile with the filename
            return fileDescriptor;
		
		FileStructure fileStructure = hashOfFileStructure.get(openfile.getName());

		if (fileStructure != null) { //FileStructure is in FileStructure hash
			if (!fileStructure.isLinked) //FileStructure is not linked
                linkedFlag = false;
			else { //FileStructure is linked
				OpenFile myOpenFile = processOfOpenFiles.get(fileStructure.fileDescriptorNum); //find the OpenFile in OpenFile hash of this UserProcess
				if (myOpenFile != null) //OpenFile found in hash, which means this UserProcess already has this OpenFile
						fileDescriptor = fileStructure.fileDescriptorNum;
				else { //OpenFile not found in hash, which means other UserProcess opened the file and this UserProcess is trying to open the file too
					if (numberOfOpenFiles < maxNumberOfOpenFiles) { //if number of files this UserProcess opened is less than max
						processOfOpenFiles.put(new Integer(fileStructure.counter), fileStructure.openfile); //add to OpenFile hash
						fileStructure.counter++;
						numberOfOpenFiles++;
						fileDescriptor = fileStructure.fileDescriptorNum;
					}
				}
			}
		}
        else if (linkedFlag == true && numberOfOpenFiles < maxNumberOfOpenFiles) {
            
        }
		// fileDescriptor still not found, and it is not marked as unlinked,
		if (fileDescriptor == -1 && linkedFlag && numberOfOpenFiles < maxNumberOfOpenFiles) {
			hashOfFileStructure.put(openfile.getName(), new FileStructure(openfile, ++fileDescriptorNum));
			processOfOpenFiles.put(new Integer(fileDescriptorNum), openfile);
			numberOfOpenFiles++;
			fileDescriptor = fileDescriptorNum;
		}
		
		return fileDescriptor;
	}
