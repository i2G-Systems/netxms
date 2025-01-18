/*
** NetXMS - Network Management System
** Copyright (C) 2023-2025 Raden Solutions
**
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 2 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
**
** File: wlcbridge.cpp
**
**/

#include "wlcbridge.h"
#include <netxms-version.h>

/**
 * Module metadata
 */
DEFINE_MODULE_METADATA("WLCBRIDGE", "Raden Solutions", NETXMS_VERSION_STRING_A, NETXMS_BUILD_TAG_A)

/**
 * Interfaces
 */
extern WirelessControllerBridge g_hfclBridge;
extern WirelessControllerBridge g_ruckusBridge;

void StopHFCLBackgroundThreads();

/**
 * Get wireless controller bridge interface
 */
static WirelessControllerBridge *GetWLCBridgeInterface(const TCHAR *bridgeName)
{
   if (!_tcsicmp(bridgeName, _T("HFCL")))
      return &g_hfclBridge;
   if (!_tcsicmp(bridgeName, _T("RUCKUS")))
      return &g_ruckusBridge;
   return nullptr;
}

/**
 * Server shutdown handler
 */
static void ProcessServerShutdown()
{
   StopHFCLBackgroundThreads();
}

/**
 * Module entry point
 */
extern "C" bool __EXPORT NXM_Register(NXMODULE *module, Config *config)
{
   module->dwSize = sizeof(NXMODULE);
   _tcscpy(module->szName, _T("WLCBRIDGE"));
   module->pfGetWLCBridgeInterface = GetWLCBridgeInterface;
   module->pfShutdown = ProcessServerShutdown;
   return true;
}

#ifdef _WIN32

/**
 * DLL Entry point
 */
BOOL WINAPI DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
   if (dwReason == DLL_PROCESS_ATTACH)
      DisableThreadLibraryCalls(hInstance);
   return TRUE;
}

#endif
