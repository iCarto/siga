/*
 * Copyright (c) 2003, 2004 Objectix Pty Ltd  All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OBJECTIX PTY LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package au.com.objectix.jgridshift.jca;

import javax.resource.cci.ResourceAdapterMetaData;

public class ResourceAdapterMetaDataImpl implements ResourceAdapterMetaData {

  public String getAdapterName() {
    return "jgridshift Adapter";
  }

  public String getAdapterShortDescription() {
    return "A resource adapter for jgridshift";
  }

  public String getAdapterVendorName() {
    return "Objectix Pty Ltd";
  }

  public String getAdapterVersion() {
    return "2.0.0";
  }

  public String[] getInteractionSpecsSupported() {
    return new String[] {GridShiftInteractionSpec.class.getName()};
  }

  public String getSpecVersion() {
    return "2.0.0";
  }

  public boolean supportsExecuteWithInputAndOutputRecord() {
    return true;
  }

  public boolean supportsExecuteWithInputRecordOnly() {
    return true;
  }

  public boolean supportsLocalTransactionDemarcation() {
    return false;
  }
}
