import { Pipe, PipeTransform } from '@angular/core';

import { humanizeEnum } from '../../core/models/enums';

/** Turns SCREAMING_SNAKE_CASE enum values into readable labels for templates. */
@Pipe({ name: 'enumLabel', standalone: true })
export class EnumLabelPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    return humanizeEnum(value);
  }
}
