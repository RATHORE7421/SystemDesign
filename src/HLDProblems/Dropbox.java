// What is Dropbox?
// Cloud based file storage that allows user to store & share files

// Requirements:
// - Can upload a file(What size max?)1️⃣
// - Can sync file across devices 4️⃣
// - Filters - created date, size, type
// - Can share file with others 3️⃣
// - Can download a file(😪)2️⃣
// -----Out of scope----- Roll own blob storage

// Non Funtional Requirements:
// - Scalability: System should support file as large as 50GB
// - CAP: Availability >> Consistency
// - Caching: Can cache file metadata
// - Latency: Can access file in 100ms - Upload & Download should be fast
// - High Data integrity(sync across devices)

// Core Entities:
// - File
// - User
// - File Metadata

// APIs: explicitly every API will be consiting of USER data via JWT or OAuth
// - Upload File = POST /files
//   BODY: File & Metadata
// - List Files with filter or a file by ID or Download File = GET /files/{fileId}
//   RESPONSE: File + Metadata  
// - To share file = POST /files/{fileId}/share
//   BODY: UserId

// Cross que in my mind:
// - How a file is getting uploaded 
    // FRONTEND: - in spring boot: via multipart/form-data)
    // BACKEND: - reads the content as bytes[] or inputStream, 
    //            three ways(Locally(disk), cloud storage(AWS S3, GCP Storage), Database(BLOB, not recommended for large file))
    // 
// - How a file is getting downloaded
// - How the data is synced across  multiple devices
// - How to store file meta data?




// - While uploading or downloading if there is a network failure, how to handle it?
// - How to handle file size limits?
    //  Upload limit needs to be set, when files could upload directly to cloud storage,
    //  Upload file in chuncks
    //     STEP1: Notify cloud that you are going to upload a file, get session ID or upload ID for it.
    //     STEP2: Divide file in chuncks & each chunk is given a partNo
    //     STEP3: Send eachpart individually, cloud takes care of the order
    //     STEP4: Once all parts are uploaded, notify cloud to its completes, cloud reassembles the file

// - How to handle file versioning? - What is file versioning?
// - How to handle file conflicts when multiple users edit the same file?
// - How to handle file sharing permissions?
// - How to handle file metadata updates?
// - How to handle file deletion?
// - How to handle file synchronization across devices?
// - How to handle file caching?
// - How to handle file security and encryption?
// - How to handle file backups and recovery?
// - How to handle file access control?
// - How to handle file indexing and searching?
// - How to handle file compression and decompression?
// - How to handle file deduplication?
// - How to handle file auditing and logging?
// - How to handle file monitoring and alerting?
// - How to handle file scalability and performance?
// - How to handle file compliance and regulatory requirements?
// - How to handle file user experience and usability?
// - How to handle file integration with other systems and services?
// - How to handle file testing and quality assurance?
// - How to handle file documentation and support?
// - How to handle file deployment and maintenance?
// - How to handle file user feedback and improvement?
// - How to handle file user authentication and authorization?
// - How to handle file user interface and user experience?
// - How to handle file user notifications and alerts?
// - How to handle file user data privacy and security?
// - How to handle file user data backup and recovery?
// - How to handle file user data compliance and regulatory requirements?
// - How to handle file user data integration with other systems and services?              

package src.HLDProblems;

public class Dropbox {
    
}
