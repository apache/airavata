/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package controller

import (
	"errors"
	"io"
	"log/slog"
	"mime"
	"net/http"
	"strconv"

	"github.com/apache/airavata/internal/httpx"

	"github.com/apache/airavata/api/data/service"
)

// 5 GB
var MaxUploadBytes int64 = 5 << 30

type DataTransferController struct {
	svc *service.DataTransferService
}

func NewDataTransferController(svc *service.DataTransferService) *DataTransferController {
	return &DataTransferController{svc: svc}
}

func (h *DataTransferController) Register(mux *http.ServeMux) {
	const base = "/api/v1/data-products/{dataProductId}/content"

	mux.HandleFunc("GET "+base, h.download)
	mux.HandleFunc("PUT "+base, h.upload)
}

func (h *DataTransferController) download(w http.ResponseWriter, r *http.Request) {
	out, err := h.svc.Download(r.Context(), r.PathValue("dataProductId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	defer out.Close()

	w.Header().Set("Content-Type", "application/octet-stream")
	w.Header().Set("Content-Length", strconv.FormatInt(out.Size, 10))
	w.Header().Set("Content-Disposition",
		mime.FormatMediaType("attachment", map[string]string{"filename": out.FileName}))

	if _, err := io.Copy(w, out); err != nil {
		slog.Error("streaming data product", "path", r.URL.Path, "error", err)
	}
}

func (h *DataTransferController) upload(w http.ResponseWriter, r *http.Request) {
	body, err := uploadBody(w, r)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	defer body.Close()

	out, err := h.svc.Upload(r.Context(), r.PathValue("dataProductId"), body)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, out)
}

func uploadBody(w http.ResponseWriter, r *http.Request) (io.ReadCloser, error) {
	if r.Body == nil {
		return nil, httpx.BadRequest("Request body is required")
	}
	r.Body = http.MaxBytesReader(w, r.Body, MaxUploadBytes)

	mediaType, _, err := mime.ParseMediaType(r.Header.Get("Content-Type"))
	if err != nil || mediaType != "multipart/form-data" {
		return r.Body, nil
	}

	parts, err := r.MultipartReader()
	if err != nil {
		return nil, httpx.BadRequest("Malformed multipart body: %v", err)
	}
	for {
		part, err := parts.NextPart()
		if errors.Is(err, io.EOF) {
			return nil, httpx.BadRequest(`Multipart upload must carry the content in a part named "file"`)
		}
		if err != nil {
			return nil, httpx.BadRequest("Malformed multipart body: %v", err)
		}
		if part.FormName() == "file" {
			return part, nil
		}
		part.Close()
	}
}
