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
package wjd.phage.menus;

import wjd.amb.AScene;
import wjd.amb.control.Controller;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput.KeyPress;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.V2;
import wjd.phage.level.LevelScene;

/** 
* @author wdyce
* @since 10-Oct-2012
*/
public class TitleScene extends AScene
{
  /* CONSTANTS */
  private static final V2 HELLO_POS = new V2(100, 100);
  
  /* NESTING */
  private static class TitleController extends Controller
  {
    // attributes
    private TitleScene title;
    
    // methods
    TitleController(TitleScene title)
    {
      this.title = title;
    }
    // overrides
    public void render(ICanvas canvas)
    {
      
    }
    // implementations
    @Override
    public EUpdateResult processKeyPress(KeyPress event)
    {    
      if(event.pressed)
      {
        if(event.key != null) switch(event.key)
        {
          case L_SHIFT:
          case R_SHIFT:
            // SHIFT to edit
            title.setNext(new LevelScene(LevelScene.EMode.EDITOR));
            return EUpdateResult.STOP;
            
          case L_ALT:
          case R_ALT:
            // ALT to play
            title.setNext(new LevelScene(LevelScene.EMode.PLAY));
            return EUpdateResult.STOP;
            
          case ESC:
            return EUpdateResult.STOP;
        }
      }

      // all clear
      return EUpdateResult.CONTINUE;
    }
  }
  
  /* METHODS */
  
  // constructors
  public TitleScene()
  {
    setController(new TitleController(this));
  }
  
  /* IMPLEMENTATIONS -- SCENE */

  @Override
  public EUpdateResult update(int t_delta)
  {
    return EUpdateResult.CONTINUE;
  }

  @Override
  public void render(ICanvas canvas)
  {
    canvas.clear();
    canvas.setColour(Colour.BLACK);
    canvas.text("Press SHIFT to edit, ALT to play", HELLO_POS);
  }
}
