#ifndef _RUNTIME_ERROR_H
#define _RUNTIME_ERROR_H

#include <stdexcept>
#include <string>
#include <cstdio>

/** A helper for converting macro contents to a string constant */
#define STR_EXPR(x) #x

/** Converts the macro contents to a string constant */
#define STR(x) STR_EXPR(x)

/** Helper macro for throwing runtime errors with a string constant body and tagging source location */
#define RuntimeError(x) { \
	constexpr const char * s = "[" __FILE__ ":" STR(__LINE__) "] " x; \
	throw runtime_error(s); \
}

/** Assertion helper for throwing a RuntimeError on false */
#define assert(x) if(!(x)) RuntimeError("Assertion failed!")

/** Helper function for printing the memory contents of a variable */
#define MEMDUMP(addr, len) { \
	printf("==== BEGIN MEMDUMP %04llu BYTES AT 0x08%llu ====", (unsigned long long)(len), (unsigned long long)(addr)); \
	for (unsigned long long MEMDUMP_i = 0; MEMDUMP_i < ((unsigned long long)len); MEMDUMP_i++) { \
		unsigned char MEMDUMP_byte = ((char*)(addr))[MEMDUMP_i]; \
		if (MEMDUMP_i % 32 == 0) \
			printf("\n"); \
		printf("%02X ", MEMDUMP_byte); \
	} \
	printf("\n==== END MEMDUMP %04llu BYTES AT 0x%08llu ======\n", (unsigned long long)(len), (unsigned long long)(addr)); \
}

#endif
