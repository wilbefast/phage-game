/*
 Copyright (C) 2012 William James Dyce

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wjd.phage;

import wjd.amb.awt.AWTAmbition;
import wjd.amb.resources.AAudioManager;
import wjd.amb.resources.ATextureManager;
import wjd.amb.resources.IResourceLoader;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.level.Tile;
import wjd.phage.menus.TitleScene;

/** 
* @author wdyce
* @since 10-Oct-2012
*/
public abstract class Main 
{
  private static IResourceLoader loadScript = new IResourceLoader()
  {
    /* IMPLEMENTS -- IRESOURCELOADER */
    @Override
    public void load(ATextureManager textureManager, AAudioManager audioManager)
    {
      // terrain tileset
      textureManager.addTexture("wall_tiles", ATextureManager.ImageFileType.PNG);
      textureManager.addTileset("walls", "wall_tiles", new Rect(0, 0, 16, 16), 5, 2);
      
      // fog tileset
      textureManager.addTexture("fog_tiles", ATextureManager.ImageFileType.PNG);
      textureManager.addTileset("fog", "fog_tiles", new Rect(0, 0, 32, 32), 8, 2);
      
      // initialise classes from resources
      Tile.getResourceHandles(textureManager);

    }
  };
  
  public static void main(String args[])
  {

    //LWJGLAmbition.launch("Phage", new V2(640, 480), new TitleScene(), loadScript);
    AWTAmbition.launch("Phage", new V2(640, 480), new TitleScene(), loadScript);
  }
}
