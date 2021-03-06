/*******************************************************************************
 *  |       o                                                                   |
 *  |    o     o       | HELYX-OS: The Open Source GUI for OpenFOAM             |
 *  |   o   O   o      | Copyright (C) 2012-2016 ENGYS                          |
 *  |    o     o       | http://www.engys.com                                   |
 *  |       o          |                                                        |
 *  |---------------------------------------------------------------------------|
 *  |   License                                                                 |
 *  |   This file is part of HELYX-OS.                                          |
 *  |                                                                           |
 *  |   HELYX-OS is free software; you can redistribute it and/or modify it     |
 *  |   under the terms of the GNU General Public License as published by the   |
 *  |   Free Software Foundation; either version 2 of the License, or (at your  |
 *  |   option) any later version.                                              |
 *  |                                                                           |
 *  |   HELYX-OS is distributed in the hope that it will be useful, but WITHOUT |
 *  |   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or   |
 *  |   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License   |
 *  |   for more details.                                                       |
 *  |                                                                           |
 *  |   You should have received a copy of the GNU General Public License       |
 *  |   along with HELYX-OS; if not, write to the Free Software Foundation,     |
 *  |   Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA            |
 *******************************************************************************/
package eu.engys.core.controller;

import static eu.engys.core.project.system.SnappyHexMeshDict.BAFFLE_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.BOUNDARY_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.CELL_ZONE_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.FACE_TYPE_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.FACE_ZONE_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.INSIDE;
import static eu.engys.core.project.system.SnappyHexMeshDict.IS_CELL_ZONE_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.LEVELS_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.MODE_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.NONE_KEY;
import static eu.engys.core.project.system.SnappyHexMeshDict.OUTSIDE_KEY;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.engys.core.controller.actions.NamingConvention;
import eu.engys.core.dictionary.Dictionary;
import eu.engys.core.project.Model;
import eu.engys.core.project.geometry.Surface;
import eu.engys.core.project.geometry.surface.Region;
import eu.engys.core.project.geometry.surface.Solid;
import eu.engys.core.project.zero.cellzones.CellZone;
import eu.engys.core.project.zero.facezones.FaceZone;
import eu.engys.core.project.zero.patches.BoundaryConditions;
import eu.engys.core.project.zero.patches.BoundaryType;
import eu.engys.core.project.zero.patches.Patch;

public class GeometryToMesh {

    public static final String SLAVE_SUFFIX = "_slave";

    private static final Logger logger = LoggerFactory.getLogger(GeometryToMesh.class);

    private Model model;
    private List<Patch> patches = new ArrayList<>();
    private List<CellZone> cellZones = new ArrayList<>();
    private List<FaceZone> faceZones = new ArrayList<>();

    private List<Surface> willBePatches = new ArrayList<>();
    private List<Surface> willBeCellZones = new ArrayList<>();
    private List<Surface> willBeFaceZones = new ArrayList<>();

    private NamingConvention naming;

    public GeometryToMesh(Model model) {
        this.model = model;
        this.naming = new DefaultNamingConvention();
    }

    public String[] listPatches() {
        extractPatchesFromGeometry();

        String[] names = new String[patches.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = patches.get(i).getName();
        }

        return names;
    }

    public void execute() {
        model.getPatches().clear();
        model.getCellZones().clear();
        model.getFaceZones().clear();

        extractPatchesFromGeometry();

        model.getPatches().addPatches(patches);
        model.getCellZones().addZones(cellZones);
        model.getFaceZones().addZones(faceZones);
    }

    private void extractPatchesFromGeometry() {
        for (Surface surface : model.getGeometry().getSurfaces()) {
            logger.debug("Surface {}: {} {} {}", surface.getName(), surface.getSurfaceDictionary(), surface.getVolumeDictionary(), surface.getZoneDictionary());

            surfaceToPatch(surface);
            surfaceToCellZone(surface);
            surfaceToFaceZone(surface);
            if (willCreatePatches(surface)) {
                zoneToPatches(surface);
            } else {
                logger.debug("'{}' does NOT become a PATCH+SLAVE", surface.getName());
            }
        }

        if (model.getGeometry().hasBlock()) {
            surfaceToPatch(model.getGeometry().getBlock());
        }
    }

    private void surfaceToPatch(Surface surface) {
        if (surface.isSingleton()) {
            addPatch(surface);
        } else if (surface.hasRegions()) {
            for (Region region : surface.getRegions()) {
                addPatch(region);
            }
        } else {
            addPatch(surface);
        }
    }

    private void surfaceToCellZone(Surface surface) {
        if (willBeACellZone(surface)) {
            String zoneName = naming.getCellZoneName(surface);
            CellZone zone = new CellZone(zoneName);
            zone.setName(zoneName);
            zone.setVisible(true);
            zone.setLoaded(true);

            logger.debug("'{}' becomes a CELLZONE with name {}", surface.getName(), zoneName);

            cellZones.add(zone);
            willBeCellZones.add(surface);
        } else {
            logger.debug("'{}' does NOT become a CELLZONE", surface.getName());
        }
    }

    private void surfaceToFaceZone(Surface surface) {
        if (willBeAFaceZone(surface)) {
            String zoneName = naming.getFaceZoneName(surface);
            FaceZone zone = new FaceZone(zoneName);
            zone.setName(zoneName);
            zone.setVisible(true);
            zone.setLoaded(true);

            logger.debug("'{}' becomes a FACEZONE with name {}", surface.getName(), zoneName);

            faceZones.add(zone);
            willBeFaceZones.add(surface);
        } else {
            logger.debug("'{}' does NOT become a FACEZONE", surface.getName());
        }
    }

    private void zoneToPatches(Surface surface) {
        if (surface.hasRegions()) {
            if (surface.isSingleton()) {
                addPatchAndSlave(surface.getRegions()[0]);
            } else if (isBaffle(surface)) {
                addPatchAndSlave(surface.getRegions()[0]);
            } else if (isBoundary(surface)) {
                for (Region region : surface.getRegions()) {
                    addPatchAndSlave(region);
                }
            }
        } else {
            addPatchAndSlave(surface);
        }
    }
    
    private boolean willBeAPatch(Surface surface) {
        boolean typePlane = surface.getType().isPlane();
        boolean singletonSTLRefinementOnly = surface.getType().isStl() && surface.isSingleton() && isSurfaceRefinementOnly(surface);
        boolean typeSolidAndParentOK = surface.getType().isSolid() && willBeAPatch(((Solid) surface).getParent());
        boolean notSolidButRefinementOnly = !surface.getType().isSolid() && isSurfaceRefinementOnly(surface);
        return typePlane || singletonSTLRefinementOnly || typeSolidAndParentOK || notSolidButRefinementOnly;
    }

    private boolean willBeACellZone(Surface surface) {
        Dictionary zoneDictionary = surface.getZoneDictionary();
        if (zoneDictionary == null) {
            return false;
        } else {
            boolean hasCellZoneField = zoneDictionary.isField(CELL_ZONE_KEY);
            boolean isCellZoneTrue = zoneDictionary.found(IS_CELL_ZONE_KEY) && zoneDictionary.lookupBoolean(IS_CELL_ZONE_KEY);
            boolean faceTypeIsNotNone = zoneDictionary.found(FACE_TYPE_KEY) && !zoneDictionary.lookup(FACE_TYPE_KEY).equals(NONE_KEY);
            return hasCellZoneField && isCellZoneTrue && faceTypeIsNotNone;
        }
    }

    private boolean willBeAFaceZone(Surface surface) {
        Dictionary zoneDictionary = surface.getZoneDictionary();
        if (zoneDictionary == null) {
            return false;
        } else {
            boolean hasFaceZoneField = zoneDictionary.isField(FACE_ZONE_KEY);
            boolean faceTypeIsNotNone = zoneDictionary.found(FACE_TYPE_KEY) && !zoneDictionary.lookup(FACE_TYPE_KEY).equals(NONE_KEY);
            return hasFaceZoneField && faceTypeIsNotNone;
        }
    }

    private boolean willCreatePatches(Surface surface) {
        Dictionary zoneDictionary = surface.getZoneDictionary();
        if (zoneDictionary == null) {
            return false;
        } else {
            boolean faceTypeIsBoundary = zoneDictionary.found(FACE_TYPE_KEY) && (zoneDictionary.lookup(FACE_TYPE_KEY).equals(BOUNDARY_KEY));
            boolean faceTypeIsBaffle = zoneDictionary.found(FACE_TYPE_KEY) && (zoneDictionary.lookup(FACE_TYPE_KEY).equals(BAFFLE_KEY));

            return faceTypeIsBoundary || faceTypeIsBaffle;
        }
    }

    private boolean isBoundary(Surface surface) {
        Dictionary zoneDictionary = surface.getZoneDictionary();
        return zoneDictionary.lookup(FACE_TYPE_KEY).equals(BOUNDARY_KEY);
    }

    private boolean isBaffle(Surface surface) {
        Dictionary zoneDictionary = surface.getZoneDictionary();
        return zoneDictionary.lookup(FACE_TYPE_KEY).equals(BAFFLE_KEY);
    }

    private void addPatch(Surface surface) {
        if (willBeAPatch(surface)) {
            String patchName = naming.getPatchName(surface);
            logger.debug("'{}' becomes a PATCH with name {}, {}", surface.getName(), patchName, surface.getSurfaceDictionary());
            patches.add(newPatch(patchName));
            willBePatches.add(surface);
        } else {
            logger.debug("'{}' does NOT become a PATCH", surface.getName());
        }
    }

    private void addPatchAndSlave(Surface surface) {
        String patchName = naming.getPatchName(surface);
        String slaveName = patchName + SLAVE_SUFFIX;
        logger.debug("'{}' becomes 2 PATCHES with name {} and {}", surface.getName(), patchName, slaveName);
        patches.add(newPatch(patchName));
        willBePatches.add(surface);
        patches.add(newPatch(slaveName));
        willBePatches.add(surface);
    }

    private Patch newPatch(String name) {
        Patch patch = new Patch(name);
        patch.setName(name);
        patch.setDictionary(new Dictionary(name));
        patch.setVisible(true);
        patch.setLoaded(true);
        patch.setEmpty(false);
        patch.setPhysicalType(BoundaryType.getDefaultType());
        patch.setBoundaryConditions(new BoundaryConditions());
        return patch;
    }

    private boolean isSurfaceRefinementOnly(Surface surface) {
        boolean surfaceRefinement = isSurfaceRefinement(surface);
        boolean volumeRefinement = isVolumeRefinement(surface);
        boolean willBeACellZone = willBeACellZone(surface);
        boolean willBeAFaceZone = willBeAFaceZone(surface);

        // System.err.println("GeometryToMesh.isSurfaceRefinementOnly() surfaceRefinement: "+surfaceRefinement+", volumeRefinement: "+volumeRefinement+", willBeACellZone: "+willBeACellZone);
        return surfaceRefinement && !volumeRefinement && !willBeACellZone && !willBeAFaceZone;
    }

    private boolean isVolumeRefinement(Surface surface) {
        Dictionary volumeDictionary = surface.getVolumeDictionary();
        return volumeDictionary != null && !volumeDictionary.isEmpty() && volumeDictionary.isField(LEVELS_KEY) && volumeDictionary.isField(MODE_KEY) && (volumeDictionary.lookup(MODE_KEY).equals(INSIDE) || volumeDictionary.lookup(MODE_KEY).equals(OUTSIDE_KEY));
    }

    private boolean isSurfaceRefinement(Surface surface) {
        Dictionary surfaceDictionary = surface.getSurfaceDictionary();
        return surfaceDictionary != null && !surfaceDictionary.isEmpty() && surfaceDictionary.isField("level");
    }

    public String getPatchName(Surface surface) {
        return naming.getPatchName(surface);
    }

    public String getCellZoneName(Surface surface) {
        return naming.getCellZoneName(surface);
    }

    public String getFaceZoneName(Surface surface) {
        return naming.getFaceZoneName(surface);
    }

    public List<Surface> getWillBePatches() {
        return willBePatches;
    }

    public List<Surface> getWillBeCellZones() {
        return willBeCellZones;
    }

    public List<Surface> getWillBeFaceZones() {
        return willBeFaceZones;
    }

}
