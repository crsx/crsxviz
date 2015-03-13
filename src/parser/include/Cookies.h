#ifndef _COOKIES_H
#define _COOKIES_H

#include <vector>
#include <string>
#include <sqlite3.h>

/** Manages the cookie string list and table creation
*   @note Defining _NO_COOKIES will bypass creation of the Cookies table
*/
class CookieManager {
public:
	/** Creates the Cookies table in the database and sets up the prepared statements for use by BlobCooke
	*   @param db A pointer to the database to store cookie info in
	*/
	static void CreateCookieTable(sqlite3* db);
	
	/** Performs a lookup of the ID for a cookie string <br>
	*   If the cookie string is not in the Cookies table then it is assigned a new ID number and added to the table
	*   @param s The cookie string to get an ID for
	*   @return The CookieID of the string passed
	*/
	static int GetCookieID(std::string s);
	
private:
	/** Prepared statement template for inserting a cookie into the database
	*/
	static constexpr const char * CookieInsertTemplate = R"(INSERT INTO `Cookies`
	(`CookieID`,`Value`) VALUES (?,?);)";

	/** SQL expression for creating the Cookies table in the database
	*/
	static constexpr const char * TableSchema = R"(CREATE TABLE `Cookies` (
	`CookieID`        INTEGER NOT NULL PRIMARY KEY UNIQUE,
	`Value`           TEXT);)";
	
	/** The list of the cookies currently in the database<br>
	*   @note The index corresponds to the CookieID in the table
	*/
	static std::vector<std::string> CookieList;
	
	/** Initializes the prepared statements for use with the specified DB
	*   @param db The database to link the prepared statements to
	*/
	static void initPreparedStatements(sqlite3* db);
	
	/** Static storage of the cookie insert prepared statement 
	*/
	static sqlite3_stmt* CookieInsertStmt;
};

/** The class that Step interfaces with for doing cookie management
*/
class BlobCookie {
public:
	/** Adds a cookie string to this instance
	*   @param s The cookie string to add
	*/
	void addCookie(std::string s);
	
	/** Gets the blob version of this instance for use by Step in adding the Cookies field of the Steps table. <br>
	*	@return A pointer to the blob to insert. (DO NOT FREE THIS)
	*   @note If _NO_COOKIES is defined then this will always return NULL
	*   The data format is an array of network byte order uint32_t[]
	*/
	void * getBlob();
	
	/** Gets the length of the memory returned by getBlob()
	*   @return the length in bytes of the memory containing the blob
	*   @note If _NO_COOKIES is defined then this will always return 0
	*/
	size_t length();
	
	/** Performs class setup
	*/
	BlobCookie();
	
	/** Performs class teardown
	*   @note Frees the memory returned by getBlob()
	*/
	~BlobCookie();
	
private:
	/** The blob of memory used to store the cookie pointers when getBlob() is called
	*/	
	char * blob;
	
	/** The list of cookie pointers contained in this BlobCookie
	*/
	std::vector<uint32_t> cookieIDs;
};
#endif
