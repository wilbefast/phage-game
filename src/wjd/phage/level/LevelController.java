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
package wjd.phage.level;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IController;
import wjd.amb.control.IDynamic;
import wjd.amb.control.IInput;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.phage.menus.TitleScene;

/**
 *
 * @author wdyce
 * @since Nov 2, 2012
 */
public abstract class LevelController implements IController, IVisible, IDynamic
{
  /* CONSTANTS */
  private static final JFileChooser fileChooser = new JFileChooser();
  private static final FileFilter levelFilter = new FileFilter()
  {
    @Override
    public boolean accept(File file)
    {
      // allow directories
      if(file == null || file.isDirectory())
        return true;
      
      String filename = file.getName(),
             extension = filename.substring(filename.lastIndexOf('.')+1);
      return (extension.equals("lvl"));
    }

    @Override
    public String getDescription()
    {
      return "Game level (LVL) files";
    }
  };
  static
  {
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.addChoosableFileFilter(levelFilter);
  }
  
  /* ATTRIBUTES */
  protected LevelScene level;
  
  /* METHODS */
  
  // constructors
  public LevelController(LevelScene level)
  {
    this.level = level;
  }
  
  /* IMPLEMENTS -- IVISIBLE */
  
  @Override
  public void render(ICanvas canvas)
  {
    // overridden if needed
  }
  
  /* OVERRIDES -- CONTROLLER */
  
  @Override
  public EUpdateResult processInput(IInput input)
  {
    // control camera
    level.getCamera().processInput(input);
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  @Override
  public EUpdateResult processKeyPress(IInput.KeyPress event)
  {
    // go back to title on ESC press
    if(event.key == IInput.EKeyCode.ESC)
    {
      level.setNext(new TitleScene());
      return EUpdateResult.REPLACE_ME;
    }
    
        
    // load and save
    if(event.pressed)
    {
      if(event.key != null) switch(event.key)
      {
        case L_CTRL:
        case R_CTRL:
          // save on CONTROL
          if(fileChooser.showSaveDialog(fileChooser) == JFileChooser.APPROVE_OPTION)
            save(fileChooser.getSelectedFile());
        break;

        case L_ALT:
        case R_ALT:
          // load on ALT
          if(fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION)
              load(fileChooser.getSelectedFile());
        break;
      }
    }
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
  
  /* SUBROUTINES */
  
  private void save(File file)
  {
    level.tilegrid.save(file);
    System.gc();
  }
  
  private void load(File file)
  {
    TileGrid backup = level.tilegrid;
    try
    {
      level.tilegrid = new TileGrid(file);
      level.fog.setGrid(level.tilegrid);
      System.gc();
    }
    catch (IOException ex)
    {
      Logger.getLogger(LevelController.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ClassNotFoundException ex)
    {
      Logger.getLogger(LevelController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
